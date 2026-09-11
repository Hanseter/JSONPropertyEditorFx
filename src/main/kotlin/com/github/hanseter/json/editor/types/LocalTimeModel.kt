package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.util.BindableJsonType
import org.everit.json.schema.StringSchema

class LocalTimeModel(override val schema: EffectiveSchema<StringSchema>) : TypeModel<String?, SupportedType.SimpleType.StringType> {
    override val supportedType: SupportedType.SimpleType.StringType
        get() = SupportedType.SimpleType.StringType
    override var bound: BindableJsonType? = null
    override val defaultValue: String?
        get() = schema.defaultValue as? String

    override val previewString: PreviewString
        get() = PreviewString.create(value, defaultValue, rawValue)

}