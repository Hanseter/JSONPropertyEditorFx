package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonType
import org.everit.json.schema.ArraySchema
import org.everit.json.schema.Schema
import org.json.JSONArray

class TupleModel(override val schema: SchemaWrapper<ArraySchema>, val contentSchemas: List<Schema>) : TypeModel<JSONArray?, SupportedType.ComplexType.TupleType> {
    override val supportedType: SupportedType.ComplexType.TupleType
        get() = SupportedType.ComplexType.TupleType
    override var bound: BindableJsonType? = null
    override val defaultValue: JSONArray?
        get() = schema.schema.defaultValue as? JSONArray

    override var value: JSONArray?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
        }

    companion object {
        val CONVERTER: (Any?) -> JSONArray? = { it as? JSONArray }
    }
}