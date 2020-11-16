package com.github.hanseter.json.editor.extensions

import org.everit.json.schema.Schema

class RegularSchemaWrapper<T : Schema>(override val parent: SchemaWrapper<*>?,
                                       override val schema: T,
                                       customTitle: String? = null) : SchemaWrapper<T> {
    override val title = customTitle ?: calcSchemaTitle(schema)

    override fun getPropertyName(): String = calcPropertyName(schema)

    companion object {
        fun calcSchemaTitle(schema: Schema) = schema.title ?: calcPropertyName(schema)

        fun calcPropertyName(schema: Schema): String = (schema.schemaLocation
                ?: schema.location.toString()).split('/').last()
    }
}