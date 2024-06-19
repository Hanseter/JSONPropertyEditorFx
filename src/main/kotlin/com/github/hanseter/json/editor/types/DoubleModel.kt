package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.util.BindableJsonType
import org.everit.json.schema.NumberSchema

class DoubleModel(override val schema: EffectiveSchema<NumberSchema>) :
    TypeModel<Double?, SupportedType.SimpleType.DoubleType> {
    override val supportedType: SupportedType.SimpleType.DoubleType
        get() = SupportedType.SimpleType.DoubleType
    override var bound: BindableJsonType? = null
    override val defaultValue: Double?
        get() = schema.defaultValue as? Double

    override var value: Double?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
        }


    override val previewString: PreviewString
        get() = PreviewString.create(
            formatDoubleToString(value),
            formatDoubleToString(defaultValue),
            rawValue
        )

    companion object {
        val CONVERTER: (Any?) -> Double? = { (it as? Number)?.toDouble() }

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