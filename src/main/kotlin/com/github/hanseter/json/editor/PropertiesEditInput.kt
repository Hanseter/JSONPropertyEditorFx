package com.github.hanseter.json.editor

import org.json.JSONObject

/**
 * The data which various user-defined functions receive as input.
 *
 * It allows access to the current document data, as well as the schema.
 * @param data The document data.
 */
data class PropertiesEditInput(val data: JSONObject, val schema: ParsedSchema)