package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.util.BindableJsonType
import org.everit.json.schema.StringSchema

class ResourceModel(
    override val schema: EffectiveSchema<StringSchema>,
) : TypeModel<String?, SupportedType.SimpleType.ResourceType> {
    override val supportedType: SupportedType.SimpleType.ResourceType
        get() = SupportedType.SimpleType.ResourceType
    override var bound: BindableJsonType? = null
    override val defaultValue: String?
        get() = schema.defaultValue as? String

    override val previewString: PreviewString
        get() = PreviewString.create(value, defaultValue, rawValue)

}