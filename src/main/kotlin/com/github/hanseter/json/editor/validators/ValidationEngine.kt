package com.github.hanseter.json.editor.validators

import com.github.hanseter.json.editor.ControlFactory
import com.github.hanseter.json.editor.IdReferenceProposalProvider
import com.github.hanseter.json.editor.SchemaNormalizer
import com.github.hanseter.json.editor.controls.TypeControl
import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.extensions.SimpleEffectiveSchema
import com.github.hanseter.json.editor.util.EditorContext
import com.github.hanseter.json.editor.util.IdRefDisplayMode
import com.github.hanseter.json.editor.util.RootBindableType
import org.everit.json.schema.ValidationException
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI

object ValidationEngine {

    fun validate(elemId: String, data: JSONObject, schema: JSONObject, resolutionScope: URI?, referenceProposalProvider: IdReferenceProposalProvider)
            : List<Pair<JSONPointer, String>> {
        val parsedSchema = SchemaNormalizer.parseSchema(schema, resolutionScope, false, referenceProposalProvider)

        val effectiveSchema = SimpleEffectiveSchema(null, parsedSchema, null)
        val control = ControlFactory.convert(effectiveSchema, EditorContext(referenceProposalProvider,
                elemId, {}, IdRefDisplayMode.ID_ONLY))
        control.bindTo(RootBindableType(data))
        return validate(control, elemId, data,
                listOf(IdReferenceValidator(referenceProposalProvider)))
    }

    fun validate(rootControl: TypeControl, id: String, data: JSONObject, customValidators: List<Validator>): List<Pair<JSONPointer, String>> {
        val controls = flattenControl(rootControl).toList()
        val toValidate = prepareForValidation(controls.asSequence().map { it.model.schema }, data)

        val errorMap = mutableMapOf<JSONPointer, MutableList<String>>()
        val parentErrorCount = mutableMapOf<JSONPointer, Int>()
        fun addError(pointer: List<String>, message: String) {
            val pointers = pointer.heads()
            pointers.dropLast(1).forEach { parentPointer ->
                val count = parentErrorCount[parentPointer] ?: 0
                parentErrorCount[parentPointer] = count + 1
            }
            errorMap.getOrPut(pointer, { mutableListOf() }).add(message)
        }
        validateSchema(toValidate, rootControl.model.schema, ::addError)
        return controls.mapNotNull { control ->
            val pointer = listOf("#") + control.model.schema.pointer
            validateCustomValidator(control, pointer, customValidators, id, ::addError)
            createErrorMessage(parentErrorCount[pointer]
                    ?: 0, errorMap[pointer])?.let { pointer to it }
        }
    }

    private fun flattenControl(toFlatten: TypeControl): Sequence<TypeControl> =
            toFlatten.childControls.asSequence().flatMap { flattenControl(it) } + sequenceOf(toFlatten)

    private fun prepareForValidation(schemas: Sequence<EffectiveSchema<*>>, data: JSONObject): JSONObject {
        val copy = deepCopyForJson(data)
        schemas.forEach { schema ->
            val defaultValue = schema.defaultValue
            if (defaultValue != null) {
                val pointer = schema.pointer
                val parent = org.json.JSONPointer(pointer.dropLast(1)).queryFrom(copy) as? JSONObject
                if (parent != null && !parent.has(pointer.last())) {
                    parent.put(pointer.last(), schema.defaultValue)
                }
            }
        }
        return copy
    }

    private fun validateSchema(data: JSONObject, schema: EffectiveSchema<*>, errorCollector: (JSONPointer, String) -> Unit) {
        try {
            schema.baseSchema.validate(data)
        } catch (e: ValidationException) {
            mapPointerToError(e, errorCollector)
        }
    }

    private fun mapPointerToError(ex: ValidationException, errorCollector: (JSONPointer, String) -> Unit) {
        fun flatten(ex: ValidationException): Sequence<ValidationException> =
                if (ex.causingExceptions.isEmpty()) sequenceOf(ex)
                else ex.causingExceptions.asSequence().flatMap { flatten(it) }

        flatten(ex).forEach { validationError ->
            errorCollector(validationError.pointerToViolation.split('/'), validationError.errorMessage)
        }
    }

    private fun validateCustomValidator(
            control: TypeControl,
            pointer: JSONPointer,
            customValidators: List<Validator>,
            id: String,
            errorCollector: (JSONPointer, String) -> Unit
    ) {
        customValidators.filter { it.selector.matches(control.model) }.forEach { validator ->
            validator.validate(control.model, id).forEach { errorCollector(pointer, it) }
        }
    }

    private fun createErrorMessage(subErrors: Int, errors: List<String>?): String? {
        val subErrorMessage = if (subErrors > 0) {
            "$subErrors sub-error" + if (subErrors > 1) "s" else ""
        } else null
        val errorMessage = errors?.joinToString("\n")
        return when {
            subErrorMessage != null && errorMessage != null -> subErrorMessage + "\n" + errorMessage
            errorMessage != null -> errorMessage
            subErrorMessage != null -> subErrorMessage
            else -> null
        }
    }
}

fun <T> List<T>.heads(): List<List<T>> {
    tailrec fun <T> headsRec(list: List<T>, heads: List<List<T>>): List<List<T>> = when {
        list.isEmpty() -> heads
        else -> headsRec(list.dropLast(1), listOf(list) + heads)
    }
    return headsRec(this, emptyList())
}

private fun <T> deepCopyForJson(obj: T): T = when (obj) {
    is JSONObject -> obj.keySet().fold(JSONObject()) { acc, it ->
        acc.put(it, deepCopyForJson(obj.get(it)))
    } as T
    is JSONArray -> obj.fold(JSONArray()) { acc, it ->
        acc.put(deepCopyForJson(it))
    } as T
    else -> obj
}

typealias JSONPointer = List<String>
