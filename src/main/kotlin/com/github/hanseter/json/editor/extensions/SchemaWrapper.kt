package com.github.hanseter.json.editor.extensions

import org.everit.json.schema.Schema

interface SchemaWrapper<T : Schema> {
    val parent: SchemaWrapper<*>?
    val schema: T
    val title: String
    val readOnly: Boolean

    fun getPropertyName(): String

    fun getPropertyOrder(): List<String> = (schema.unprocessedProperties["order"] as? Iterable<*>)?.filterIsInstance<String>()?.toList()?.distinct()
            ?: emptyList()
}