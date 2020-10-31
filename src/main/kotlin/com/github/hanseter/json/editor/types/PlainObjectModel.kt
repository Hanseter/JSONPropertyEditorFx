package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonType
import org.everit.json.schema.ObjectSchema
import org.json.JSONObject

class PlainObjectModel(override val schema: SchemaWrapper<ObjectSchema>) : TypeModel<JSONObject?> {
    override var bound: BindableJsonType? = null
    override val defaultValue: JSONObject?
        get() = schema.schema.defaultValue as? JSONObject

    override var value: JSONObject?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
        }

    override val validationErrors: List<String> = emptyList()

    companion object {
        val CONVERTER: (Any?) -> JSONObject? = { it as? JSONObject }
    }
}