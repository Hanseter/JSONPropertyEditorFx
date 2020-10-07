package com.github.hanseter.json.editor.extensions

import org.everit.json.schema.ArraySchema
import org.everit.json.schema.Schema

class ArraySchemaWrapper<T : Schema>(override val parent: SchemaWrapper<ArraySchema>, override val schema: T, index: Int) : SchemaWrapper<T> {
    override val title = index.toString()

    override fun getPropertyName(): String = title

}