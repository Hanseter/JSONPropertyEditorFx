package com.github.hanseter.json.editor.extensions

import org.everit.json.schema.Schema

class RegularSchemaWrapper<T : Schema>(override val parent: SchemaWrapper<*>?,
                                       override val schema: T,
                                       customTitle: String? = null) : SchemaWrapper<T> {
    override val title = customTitle ?: schema.title ?: getPropertyName()

    override fun getPropertyName(): String = (schema.schemaLocation
            ?: schema.location.toString()).split('/').last()

}
