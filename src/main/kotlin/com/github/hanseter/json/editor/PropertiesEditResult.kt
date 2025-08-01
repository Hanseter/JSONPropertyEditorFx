package com.github.hanseter.json.editor

import org.json.JSONObject

/**
 * The result of a user function to change the editor's state.
 */
data class PropertiesEditResult @JvmOverloads constructor(

    /**
     * The new document data.
     */
    val data: JSONObject,

    /**
     * The new schema, or `null` if the schema shouldn't change.
     *
     * A non-null value should only be passed here if the schema should actually change; if the schema hasn't changed, `null` should be passed here, not the old schema.
     */
    val schema: ParsedSchema? = null
)
