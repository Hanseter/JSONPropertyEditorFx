package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import org.everit.json.schema.StringSchema

class StringModel(
    override val schema: EffectiveSchema<StringSchema>,
    private val context: EditorContext
) : TypeModel<String?, SupportedType.SimpleType.StringType> {
    override val supportedType: SupportedType.SimpleType.StringType
        get() = SupportedType.SimpleType.StringType
    override var bound: BindableJsonType? = null
    override val defaultValue: String?
        get() = context.customizationObject().getNullStringPreview(this)

    override val previewString: PreviewString
        get() = PreviewString.create(value, defaultValue, rawValue)

}