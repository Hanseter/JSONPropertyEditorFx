package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonType
import org.everit.json.schema.NumberSchema

class IntegerModel(override val schema: SchemaWrapper<NumberSchema>) : TypeModel<Int?> {
    override var bound: BindableJsonType? = null
    override val defaultValue: Int?
        get() = schema.schema.defaultValue as? Int

    override var value: Int?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
        }

    override val validationErrors: List<String> = emptyList()

    companion object {
        val CONVERTER: (Any?) -> Int? = { (it as? Number)?.toInt() }
    }
}