package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.types.TypeModel
import org.json.JSONArray
import org.json.JSONObject

object ResetToDefaultAction : EditorAction {
    override val text: String = "↻"
    override val description: String = "Reset to default"
    override val selector: ActionTargetSelector = ActionTargetSelector {
        it.schema.schema.defaultValue != null && !it.schema.readOnly
    }

    override fun apply(currentData: JSONObject, model: TypeModel<*, *>): JSONObject? {
        val key = model.schema.getPropertyName()
        when (val parentContainer = model.schema.parent?.extractProperty(currentData)
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