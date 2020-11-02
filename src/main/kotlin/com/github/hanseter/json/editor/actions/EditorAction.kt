package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.types.TypeModel
import org.json.JSONObject

/**
 *
 */
interface EditorAction {

    val text: String

    val description: String

    val selector: ActionTargetSelector

    fun matches(model: TypeModel<*, *>) = selector.matches(model)

    fun apply(currentData: JSONObject, model: TypeModel<*, *>): JSONObject?

    fun shouldBeDisabled(model: TypeModel<*, *>): Boolean =
            model.schema.readOnly
}