package com.github.hanseter.json.editor.extensions

import org.everit.json.schema.ReferenceSchema
import org.everit.json.schema.Schema

class ReferredSchemaWrapper<T : Schema>(override val parent: SchemaWrapper<ReferenceSchema>, override val schema: T, customTitle: String? = null) : SchemaWrapper<T> {
    override val title = customTitle ?: parent.title ?: schema.title ?: getPropertyName()

    override val pointer: List<String>
        get() = parent.pointer

    override fun getPropertyName(): String = parent.getPropertyName()
}