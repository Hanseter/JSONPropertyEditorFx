package com.github.hanseter.json.editor.extensions

import org.everit.json.schema.CombinedSchema
import org.everit.json.schema.Schema

/**
 * This wrapper is used to wrap schemas that come from a combined schema (e.g. allOf[]).
 * These schemas do not need to be included when building a json pointer.
 */
class CombinedSchemaWrapper<T : Schema>(override val parent: SchemaWrapper<CombinedSchema>, override val schema: T) : SchemaWrapper<T> {
    override val title: String
        get() = RegularSchemaWrapper.calcSchemaTitle(schema)

    override val pointer: List<String>
        get() = parent.pointer

    override fun getPropertyName(): String = parent.getPropertyName()
}