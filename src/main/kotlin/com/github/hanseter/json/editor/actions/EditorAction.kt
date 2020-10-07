package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import org.json.JSONObject

/**
 *
 */
interface EditorAction {

    val text: String

    val description: String

    val selector: ActionTargetSelector

    fun matches(schema: SchemaWrapper<*>) = selector.matches(schema)

    fun apply(currentData: JSONObject, schema: SchemaWrapper<*>): JSONObject?

    fun shouldBeDisabled(schema: SchemaWrapper<*>): Boolean =
            schema.readOnly
}