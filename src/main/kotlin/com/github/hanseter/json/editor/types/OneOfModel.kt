package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.controls.TypeControl
import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.extensions.EffectiveSchemaOfCombination
import com.github.hanseter.json.editor.extensions.SimpleEffectiveSchema
import com.github.hanseter.json.editor.merge
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import javafx.util.StringConverter
import org.everit.json.schema.CombinedSchema
import org.everit.json.schema.ObjectSchema
import org.everit.json.schema.Schema
import org.everit.json.schema.ValidationException
import org.json.JSONObject

open class OneOfModel(
    override val schema: EffectiveSchema<CombinedSchema>,
    val editorContext: EditorContext
) : TypeModel<Any?, SupportedType.ComplexType.OneOfType> {
    override val supportedType: SupportedType.ComplexType.OneOfType
        get() = SupportedType.ComplexType.OneOfType
    override var bound: BindableJsonType? = null
        set(value) {
            field = value
            onBoundChanged(value)
        }

    //for now no support for default values in oneOfs
    override val defaultValue: Any?
        get() = null
    override var value: Any?
        get() = bound?.getValue(schema)
        set(value) {
            bound?.setValue(schema, value)
        }

    var actualType: TypeControl? = null
        private set

    val objectOptionData = JSONObject()
//    val optionData = HashMap<Schema, Any>()

    /**
     * Whether the current actualType was set manually by the user. In that case, it should not be
     * changed even if the data does not validate.
     */
    private var typeSetManually = false

    private fun onBoundChanged(new: BindableJsonType?) {
        if (new == null) {
            actualType = null
            return
        }
        if (new.getValue(schema) == null) {
            actualType = null
            return
        }
        val value = value ?: return
        if (!typeSetManually
            && (actualType == null || !isValid(actualType!!.model.schema.baseSchema, value))
        ) {
            val possibleNewType = tryGuessActualTypeControl()
            if (possibleNewType != null) {
                actualType = possibleNewType
                possibleNewType.bindTo(new)
            }
        }
        typeSetManually = false
    }

    protected open fun isValid(schema: Schema, data: Any): Boolean =
        try {
            schema.validate(data)
            true
        } catch (e: ValidationException) {
            false
        }

    private fun tryGuessActualTypeControl(): TypeControl? =
        tryGuessActualSchema(value).second?.let {
            createActualControl(it)
        }


    protected open fun tryGuessActualSchema(value: Any?): Pair<Int, Schema?> {
        val data = value ?: return -1 to null
        val found = schema.baseSchema.subschemas.find { isValid(it, data) }
        val index = schema.baseSchema.subschemas.indexOf(found)
        return index to found
    }

    protected fun createActualControl(subSchema: Schema): TypeControl =
        editorContext.controlFactory.create(
            EffectiveSchemaOfCombination(schema, subSchema),
            editorContext
        )

    fun selectType(schema: Schema?) {
        if (schema == null) return
        (value as? JSONObject)?.also { merge(objectOptionData, it) }
        actualType =
            editorContext.controlFactory.create(
                EffectiveSchemaOfCombination(this.schema, schema),
                editorContext
            )
        typeSetManually = true
        if (schema is ObjectSchema) {
            val keysToRemove = this.schema.baseSchema.subschemas
                .filterIsInstance<ObjectSchema>()
                .flatMap { it.propertySchemas.keys }
                .toSet()
            val keysToKeep = schema.propertySchemas.keys
            value = produceValueForNewType(schema, keysToRemove, keysToKeep)
        }
        bound?.also { actualType?.bindTo(it) }
    }

    protected open fun produceValueForNewType(
        schema: ObjectSchema,
        keysToRemove: Set<String>,
        keysToKeep: Set<String>
    ): JSONObject? {
        return merge(JSONObject(), objectOptionData, keysToRemove - keysToKeep)
    }

    companion object {
        object SchemaTitleStringConverter : StringConverter<Schema>() {
            override fun toString(obj: Schema?): String? =
                obj?.let { SimpleEffectiveSchema.calcSchemaTitle(it) }

            override fun fromString(string: String?): Schema? = null
        }

        private fun schemaToString(pair: Pair<Int, Schema?>): String? {
            if (pair.second == null) return null
            return pair.second?.title ?: pair.first.toString()
        }
    }

    override val previewString: PreviewString
        get() = PreviewString.createPseudo(
            schemaToString(tryGuessActualSchema(value)),
            schemaToString(tryGuessActualSchema(defaultValue)),
            rawValue
        )
}