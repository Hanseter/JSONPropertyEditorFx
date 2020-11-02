package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonType

class UnsupportedTypeModel(override val schema: SchemaWrapper<*>) : TypeModel<Any?, SupportedType.SimpleType.UnsupportedType> {
    override val supportedType: SupportedType.SimpleType.UnsupportedType
        get() = SupportedType.SimpleType.UnsupportedType
    override var bound: BindableJsonType? = null
    override val defaultValue: Any?
        get() = schema.schema.defaultValue

    override var value: Any?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
        }

    companion object {
        val CONVERTER: (Any?) -> Any? = { it }
    }
}