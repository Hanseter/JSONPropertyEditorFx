package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import com.github.hanseter.json.editor.util.IdRefDisplayMode
import com.github.hanseter.json.editor.util.PropertyGrouping
import com.github.hanseter.json.editor.util.ViewOptions
import org.everit.json.schema.StringSchema

class ResourceModel(
    override val schema: EffectiveSchema<StringSchema>,
) : TypeModel<String?, SupportedType.SimpleType.ResourceType> {
    override val supportedType: SupportedType.SimpleType.ResourceType
        get() = SupportedType.SimpleType.ResourceType
    override var bound: BindableJsonType? = null
    override val defaultValue: String?
        get() = schema.defaultValue as? String

    override var value: String?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
        }
    override val previewString: PreviewString
        get() = PreviewString.create(value, defaultValue, rawValue)

    companion object {
        val CONVERTER: (Any) -> String = { it as? String ?: it.toString() }
    }

}