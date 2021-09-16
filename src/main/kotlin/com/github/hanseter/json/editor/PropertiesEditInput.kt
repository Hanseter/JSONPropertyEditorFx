package com.github.hanseter.json.editor

import org.json.JSONObject

/**
 * The data which various user-defined functions receive as input.
 *
 * It allows access to the current document data, as well as the schema.
 * @param data The document data.
 */
data class PropertiesEditInput(
        val data: JSONObject,
        private val _schema: JSONObject) {

    /**
     * Gets a copy of the current schema.
     */
    val schema
        get() = SchemaNormalizer.deepCopy(_schema) as JSONObject

}