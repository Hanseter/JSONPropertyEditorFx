package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import org.everit.json.schema.NumberSchema

open class IntegerModel(override val schema: EffectiveSchema<NumberSchema>, private val context: EditorContext) :
    TypeModel<Int?, SupportedType.SimpleType.IntType> {
    override val supportedType: SupportedType.SimpleType.IntType
        get() = SupportedType.SimpleType.IntType
    override var bound: BindableJsonType? = null
    override val defaultValue: Int?
        get() = context.customizationObject().getNullIntPreview(this)

    override val previewString: PreviewString
        get() = PreviewString.create(value?.toString(), defaultValue?.toString(), rawValue)
}