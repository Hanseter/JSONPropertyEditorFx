package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonType
import org.everit.json.schema.ArraySchema
import org.everit.json.schema.Schema
import org.json.JSONArray
import org.json.JSONObject

class ArrayModel(override val schema: SchemaWrapper<ArraySchema>, val contentSchema: Schema) : TypeModel<JSONArray?> {
    override var bound: BindableJsonType? = null
    override val defaultValue: JSONArray?
        get() = schema.schema.defaultValue as? JSONArray

    override var value: JSONArray?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
        }

    override val validationErrors: List<String> = emptyList()


    fun addItemAt(position: Int) {
        var children = value

        if (children == null) {
            children = JSONArray()
        }

        children.put(position, JSONObject.NULL)
        value = children
    }

    fun removeItem(index: Int) {
        val children = value ?: return
        children.remove(index)
        value = children
    }

    fun moveItemUp(index: Int) {
        val children = value ?: return
        if (index == 0) return
        val tmp = children.get(index - 1)
        children.put(index - 1, children.get(index))
        children.put(index, tmp)
        value = children
    }

    fun moveItemDown(index: Int) {
        val children = value ?: return
        if (index >= children.length() - 1) return
        val tmp = children.get(index + 1)
        children.put(index + 1, children.get(index))
        children.put(index, tmp)
        value = children
    }

    companion object {
        val CONVERTER: (Any?) -> JSONArray? = { it as? JSONArray }
    }
}