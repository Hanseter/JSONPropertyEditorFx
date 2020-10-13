package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import org.json.JSONArray
import org.json.JSONObject

object ResetToDefaultAction : EditorAction {
    override val text: String = "↻"
    override val description: String = "Reset to default"
    override val selector: ActionTargetSelector = ActionTargetSelector.Custom {
        it.schema.defaultValue != null
    }

    override fun apply(currentData: JSONObject, schema: SchemaWrapper<*>): JSONObject? {
        val key = schema.getPropertyName()
        when (val parentContainer = schema.parent?.extractProperty(currentData)
                ?: currentData) {
            null -> {
            }
            is JSONObject -> parentContainer.remove(key)
            is JSONArray -> parentContainer.put(key.toInt(), JSONObject.NULL)
            else -> throw IllegalStateException("Unknown parent container type: ${parentContainer::class.java}")
        }
        return currentData
    }
}