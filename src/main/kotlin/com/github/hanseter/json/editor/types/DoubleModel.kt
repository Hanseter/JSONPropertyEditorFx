package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonType
import org.everit.json.schema.NumberSchema

class DoubleModel(override val schema: SchemaWrapper<NumberSchema>) : TypeModel<Double?> {
    override var bound: BindableJsonType? = null
    override val defaultValue: Double?
        get() = schema.schema.defaultValue as? Double

    override var value: Double?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
        }

    override val validationErrors: List<String> = emptyList()

    companion object {
        val CONVERTER: (Any?) -> Double? = { (it as? Number)?.toDouble() }
    }
}