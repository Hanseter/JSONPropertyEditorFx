package com.github.hanseter.json.editor.extensions

import org.everit.json.schema.Schema
import org.json.JSONArray
import org.json.JSONObject

interface SchemaWrapper<T : Schema> {
    val parent: SchemaWrapper<*>?
    val schema: T
    val title: String
    val readOnly: Boolean
        get() = (parent?.readOnly ?: false) || (schema.isReadOnly ?: false)

    fun getPropertyName(): String

    fun getPropertyOrder(): List<String> = (schema.unprocessedProperties["order"] as? Iterable<*>)?.filterIsInstance<String>()?.toList()?.distinct()
            ?: emptyList()

    fun extractProperty(json: JSONObject): Any? =
            parent?.let {
                when (val parentContainer = it.extractProperty(json)) {
                    null -> null
                    is JSONObject -> parentContainer.get(getPropertyName())
                    is JSONArray -> parentContainer.get(getPropertyName().toInt())
                    else -> throw IllegalStateException("Unknown parent container type: ${parentContainer::class.java}")
                }
            } ?: json
}