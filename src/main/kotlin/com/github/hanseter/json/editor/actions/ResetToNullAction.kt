package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import org.json.JSONArray
import org.json.JSONObject

class ResetToNullAction : EditorAction {
    override val text: String = "Ø"
    override val description: String = "Reset to null"
    override val selector: ActionTargetSelector = ActionTargetSelector.AllOf(listOf(
            ActionTargetSelector.Required().invert(),
            ActionTargetSelector.SchemaType("object").invert()
    ))

    override fun apply(currentData: JSONObject, schema: SchemaWrapper<*>): JSONObject {
        val key = schema.getPropertyName()
        when (val parentContainer = schema.parent?.extractProperty(currentData)
                ?: currentData) {
            null -> {
            }
            is JSONObject -> parentContainer.put(key, JSONObject.NULL)
            is JSONArray -> parentContainer.put(key.toInt(), JSONObject.NULL)
            else -> throw IllegalStateException("Unknown parent container type: ${parentContainer::class.java}")
        }
        return currentData
    }

}