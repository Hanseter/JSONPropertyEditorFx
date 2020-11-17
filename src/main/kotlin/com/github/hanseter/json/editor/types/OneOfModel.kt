package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.ControlFactory
import com.github.hanseter.json.editor.controls.TypeControl
import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.extensions.EffectiveSchemaOfCombination
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import org.everit.json.schema.CombinedSchema
import org.everit.json.schema.Schema
import org.everit.json.schema.ValidationException

class OneOfModel(override val schema: EffectiveSchema<CombinedSchema>, val editorContext: EditorContext) : TypeModel<Any?, SupportedType.ComplexType.OneOfType> {
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

    private fun onBoundChanged(new: BindableJsonType?) {
        if (new == null) {
            actualType = null
            return
        }
        val value = value ?: return
        if (actualType == null || !isValid(actualType!!.model.schema.baseSchema, value)) {
            val possibleNewType = tryGuessActualType()
            if (possibleNewType != null) {
                actualType = possibleNewType
                possibleNewType.bindTo(new)
            }
        }
    }

    private fun isValid(schema: Schema, data: Any): Boolean =
            try {
                schema.validate(data)
                true
            } catch (e: ValidationException) {
                false
            }

    private fun tryGuessActualType(): TypeControl? {
        val data = value ?: return null
        return schema.baseSchema.subschemas.find { isValid(it, data) }?.let {
            ControlFactory.convert(EffectiveSchemaOfCombination(schema, it), editorContext)
        }
    }

    fun selectType(schema: Schema?) {
        if (schema == null) return
        actualType = ControlFactory.convert(EffectiveSchemaOfCombination(this.schema, schema), editorContext)
        bound?.also { actualType?.bindTo(it) }
    }
}