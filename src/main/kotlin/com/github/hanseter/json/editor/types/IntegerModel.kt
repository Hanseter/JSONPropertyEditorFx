package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.util.BindableJsonType
import org.everit.json.schema.NumberSchema

open class IntegerModel(override val schema: EffectiveSchema<NumberSchema>) :
    TypeModel<Int?, SupportedType.SimpleType.IntType> {
    override val supportedType: SupportedType.SimpleType.IntType
        get() = SupportedType.SimpleType.IntType
    override var bound: BindableJsonType? = null
    override val defaultValue: Int?
        get() = schema.defaultValue as? Int

    override var value: Int?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
        }

    companion object {
        val CONVERTER: (Any?) -> Int? = { (it as? Number)?.toInt() }
    }

    override val previewString: PreviewString
        get() = when {
            value != null -> PreviewString(value.toString())
            defaultValue != null -> PreviewString(defaultValue.toString(), isDefaultValue = true)
            else -> PreviewString.NO_VALUE
        }
}