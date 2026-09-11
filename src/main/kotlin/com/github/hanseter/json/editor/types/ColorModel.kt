package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import org.everit.json.schema.StringSchema

class ColorModel(
    override val schema: EffectiveSchema<StringSchema>,
    private val context: EditorContext
) : TypeModel<String?, SupportedType.SimpleType.ColorType> {
    override val supportedType: SupportedType.SimpleType.ColorType
        get() = SupportedType.SimpleType.ColorType
    override var bound: BindableJsonType? = null
    override val defaultValue: String?
        get() = context.customizationObject().getNullColorPreview(this)

    override val previewString: PreviewString
        get() = PreviewString.create(value, defaultValue, rawValue)
}