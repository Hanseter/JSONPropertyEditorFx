package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.util.BindableJsonType
import org.everit.json.schema.ArraySchema
import org.everit.json.schema.Schema
import org.json.JSONArray

class ArrayModel(override val schema: EffectiveSchema<ArraySchema>, val contentSchema: Schema) : TypeModel<JSONArray?, SupportedType.ComplexType.ArrayType> {
    override val supportedType: SupportedType.ComplexType.ArrayType
        get() = SupportedType.ComplexType.ArrayType
    override var bound: BindableJsonType? = null
    override val defaultValue: JSONArray?
        get() = schema.defaultValue as? JSONArray

    override var value: JSONArray?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
        }

    companion object {
        val CONVERTER: (Any?) -> JSONArray? = { it as? JSONArray }
    }
}