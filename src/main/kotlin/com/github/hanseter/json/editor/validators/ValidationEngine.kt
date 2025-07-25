package com.github.hanseter.json.editor.validators

import com.github.hanseter.json.editor.*
import com.github.hanseter.json.editor.controls.TypeControl
import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.extensions.SimpleEffectiveSchema
import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import com.github.hanseter.json.editor.util.EditorContext
import com.github.hanseter.json.editor.util.IdRefDisplayMode
import com.github.hanseter.json.editor.util.RootBindableType
import org.controlsfx.validation.Severity
import org.everit.json.schema.ValidationException
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.text.DecimalFormat

/**
 * This class can be used to run the same validation headlessly that would be used when displaying the data with the schema in the properties view.
 * It is safe to run the validation from another thread than the FX thread.
 */
object ValidationEngine {

    /**
     * Validates the provided [data] against the provided [schema]. Performs the same validation as it would, if  the UI was shown.
     * Even though a [PropertyControlFactory] can be provided and will be used, the method [TypeControl.createLazyControl] to actually create an fx control will never be called.
     */
    @JvmOverloads
    fun validateData(
        elemId: String,
        data: JSONObject,
        schema: JSONObject,
        resolutionScope: URI?,
        referenceProposalProvider: IdReferenceProposalProvider,
        controlFactory: PropertyControlFactory = ControlFactory
    ): List<Pair<JSONPointer, List<Validator.ValidationResult>>> {
        return validateData(
            elemId,
            data,
            ParsedSchema.create(schema, resolutionScope) ?: return emptyList(),
            referenceProposalProvider,
            controlFactory
        )
    }

    /**
     * Validates the provided [data] against the provided [schema]. Performs the same validation as it would, if  the UI was shown.
     * Even though a [PropertyControlFactory] can be provided and will be used, the method [TypeControl.createLazyControl] to actually create an fx control will never be called.
     */
    fun validateData(
        elemId: String,
        data: JSONObject,
        schema: ParsedSchema,
        referenceProposalProvider: IdReferenceProposalProvider,
        controlFactory: PropertyControlFactory
    ): List<Pair<JSONPointer, List<Validator.ValidationResult>>> {
        val effectiveSchema = SimpleEffectiveSchema(null, schema.parsed, null)
        val control = controlFactory.create(
            effectiveSchema,
            EditorContext(
                { referenceProposalProvider },
                elemId,
                {},
                IdRefDisplayMode.ID_ONLY,
                DecimalFormat().decimalFormatSymbols,
                controlFactory
            )
        )
        control.bindTo(RootBindableType(data))
        return validateData(
            control, elemId, data, listOf(IdReferenceValidator { referenceProposalProvider })
        )
    }

    @Deprecated(message = "Use validateData instead")
    fun validate(
        elemId: String,
        data: JSONObject,
        schema: JSONObject,
        resolutionScope: URI?,
        referenceProposalProvider: IdReferenceProposalProvider
    ): List<Pair<JSONPointer, List<String>>> {
        return validateData(elemId, data, schema, resolutionScope, referenceProposalProvider).map {
            it.first to it.second.map { it.message }
        }
    }

    fun validateData(
        rootControl: TypeControl,
        id: String,
        data: JSONObject,
        customValidators: List<Validator>
    ): List<Pair<JSONPointer, List<Validator.ValidationResult>>> {
        val controls = flattenControl(rootControl).toList()
        val toValidate = prepareForValidation(controls.asSequence().map { it.model.schema }, data)

        val errorMap = mutableMapOf<JSONPointer, MutableList<Validator.ValidationResult>>()
        val parentErrorCount = mutableMapOf<JSONPointer, Int>()
        fun addError(pointer: List<String>, result: Validator.ValidationResult) {
            if (result.severity == Severity.ERROR) {
                val pointers = pointer.heads()
                pointers.dropLast(1).forEach { parentPointer ->
                    val count = parentErrorCount[parentPointer] ?: 0
                    parentErrorCount[parentPointer] = count + 1
                }
            }
            errorMap.getOrPut(pointer) { mutableListOf() }.add(result)
        }
        validateSchema(toValidate, rootControl.model.schema, ::addError)
        return controls.mapNotNull { control ->
            val pointer = listOf("#") + control.model.schema.pointer
            validateCustomValidator(control, pointer, customValidators, id, ::addError)
            createErrorMessage(parentErrorCount[pointer] ?: 0, errorMap[pointer])
                ?.let { pointer to it }
        }
    }

    @Deprecated(message = "Use validateData instead")
    fun validate(
        rootControl: TypeControl,
        id: String,
        data: JSONObject,
        customValidators: List<Validator>
    ): List<Pair<JSONPointer, List<String>>> {
        return validateData(rootControl, id, data, customValidators).map {
            it.first to it.second.map { it.message }
        }
    }

    private fun flattenControl(toFlatten: TypeControl): Sequence<TypeControl> =
        toFlatten.childControls.asSequence().flatMap { flattenControl(it) } + sequenceOf(toFlatten)

    private fun prepareForValidation(
        schemas: Sequence<EffectiveSchema<*>>,
        data: JSONObject
    ): JSONObject {
        val copy = deepCopyForJson(data)
        schemas.forEach { schema ->
            val defaultValue = schema.defaultValue
            if (defaultValue != null) {
                val pointer = schema.pointer
                val parent =
                    org.json.JSONPointer(pointer.dropLast(1)).queryFrom(copy) as? JSONObject
                if (parent != null && !parent.has(pointer.last())) {
                    parent.put(pointer.last(), schema.defaultValue)
                }
            }
        }
        return copy
    }

    private fun validateSchema(
        data: JSONObject,
        schema: EffectiveSchema<*>,
        errorCollector: (JSONPointer, Validator.ValidationResult) -> Unit
    ) {
        try {
            schema.schemaForValidation.validate(data)
        } catch (e: ValidationException) {
            mapPointerToError(e, errorCollector)
        }
    }

    private fun mapPointerToError(
        ex: ValidationException,
        errorCollector: (JSONPointer, Validator.ValidationResult) -> Unit
    ) {
        fun flatten(ex: ValidationException): Sequence<ValidationException> =
            if (ex.causingExceptions.isEmpty()) sequenceOf(ex)
            else ex.causingExceptions.asSequence().flatMap { flatten(it) }

        flatten(ex).forEach { validationError ->
            errorCollector(
                validationError.pointerToViolation.split('/'),
                Validator.SimpleValidationResult(
                    Severity.ERROR,
                    validationError.errorMessage
                )
            )
        }
    }

    private fun validateCustomValidator(
        control: TypeControl,
        pointer: JSONPointer,
        customValidators: List<Validator>,
        id: String,
        errorCollector: (JSONPointer, Validator.ValidationResult) -> Unit
    ) {
        customValidators.filter { it.selector.matches(control.model) }.forEach { validator ->
            validator.validate(control.model, id).forEach { errorCollector(pointer, it) }
        }
    }

    private fun createErrorMessage(
        subErrors: Int,
        errors: List<Validator.ValidationResult>?
    ): List<Validator.ValidationResult>? {
        if (subErrors < 1) return errors
        val subErrorMessage = listOf(
            Validator.SimpleValidationResult(
                Severity.ERROR, JsonPropertiesMl.validatorSubErrors(subErrors)
            )
        )

        return if (errors == null) subErrorMessage
        else subErrorMessage + errors
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
