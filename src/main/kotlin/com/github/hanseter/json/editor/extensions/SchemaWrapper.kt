package com.github.hanseter.json.editor.extensions

import org.everit.json.schema.JSONPointer
import org.everit.json.schema.Schema
import org.json.JSONObject

interface SchemaWrapper<T : Schema> {
    val parent: SchemaWrapper<*>?
    val schema: T
    val title: String
    val readOnly: Boolean
        get() = (parent?.readOnly ?: false) || (schema.isReadOnly ?: false)
    val pointer: List<String>
        get() = parent?.pointer?.let { it + getPropertyName() } ?: emptyList()

    fun getPropertyName(): String

    fun getPropertyOrder(): List<String> = (schema.unprocessedProperties["order"] as? Iterable<*>)?.filterIsInstance<String>()?.toList()?.distinct()
            ?: emptyList()


    fun extractProperty(json: JSONObject): Any? =
            JSONPointer(pointer).queryFrom(json)
}