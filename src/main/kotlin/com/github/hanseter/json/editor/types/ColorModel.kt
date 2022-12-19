package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.util.BindableJsonType
import org.everit.json.schema.StringSchema

class ColorModel(override val schema: EffectiveSchema<StringSchema>) : TypeModel<String?, SupportedType.SimpleType.ColorType> {
    override val supportedType: SupportedType.SimpleType.ColorType
        get() = SupportedType.SimpleType.ColorType
    override var bound: BindableJsonType? = null
    override val defaultValue: String?
        get() = schema.defaultValue as? String

    override var value: String?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, StringModel.CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
        }
    override val previewString: PreviewString
        get() = PreviewString.create(value, defaultValue, rawValue)
}