package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonType

class UnsupportedTypeModel(override val schema: SchemaWrapper<*>) : TypeModel<Any?> {
    override var bound: BindableJsonType? = null
    override val defaultValue: Any?
        get() = schema.schema.defaultValue

    override var value: Any?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
        }

    override val validationErrors: List<String> = emptyList()

    companion object {
        val CONVERTER: (Any?) -> Any? = { it }
    }
}