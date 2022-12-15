package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import com.github.hanseter.json.editor.util.BindableJsonType
import org.everit.json.schema.BooleanSchema

class BooleanModel(override val schema: EffectiveSchema<BooleanSchema>) :
    TypeModel<Boolean?, SupportedType.SimpleType.BooleanType> {
    override val supportedType: SupportedType.SimpleType.BooleanType
        get() = SupportedType.SimpleType.BooleanType
    override var bound: BindableJsonType? = null
    override val defaultValue: Boolean?
        get() = schema.defaultValue as? Boolean

    override var value: Boolean?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
        }
    override val previewString: PreviewString
        get() = when {
            value != null -> PreviewString(boolToString(value))
            defaultValue != null -> PreviewString(boolToString(defaultValue), isDefaultValue = true)
            else -> PreviewString.NO_VALUE
        }

    companion object {
        val CONVERTER: (Any?) -> Boolean? = { it as? Boolean }
        private fun boolToString(boolean: Boolean?): String {
            if (boolean == null) return ""

            return if (boolean) {
                JsonPropertiesMl.bundle.getString("jsonEditor.model.boolean.true")
            } else {
                JsonPropertiesMl.bundle.getString("jsonEditor.model.boolean.false")
            }
        }

    }
}