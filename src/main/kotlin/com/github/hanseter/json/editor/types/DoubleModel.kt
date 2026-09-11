package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import org.everit.json.schema.NumberSchema

class DoubleModel(override val schema: EffectiveSchema<NumberSchema>, private val context: EditorContext) :
    TypeModel<Double?, SupportedType.SimpleType.DoubleType> {
    override val supportedType: SupportedType.SimpleType.DoubleType
        get() = SupportedType.SimpleType.DoubleType
    override var bound: BindableJsonType? = null
    override val defaultValue: Double?
        get() = context.customizationObject().getNullDoublePreview(this)

    override val previewString: PreviewString
        get() = PreviewString.create(
            formatDoubleToString(value),
            formatDoubleToString(defaultValue),
            rawValue
        )

    companion object {
        private fun formatDoubleToString(value: Double?): String? {
            if (value == null) return null

            return if ((value % 1) == 0.0) {
                value.toInt().toString()
            } else {
                value.toString()
            }
        }
    }
}