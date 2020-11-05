package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.types.TypeModel
import javafx.event.Event
import org.json.JSONArray
import org.json.JSONObject

object ResetToDefaultAction : EditorAction {
    override val text: String = "↻"
    override val description: String = "Reset to default"
    override val selector: TargetSelector = TargetSelector {
        it.schema.schema.defaultValue != null && !it.schema.readOnly
    }

    override fun apply(currentData: JSONObject, model: TypeModel<*, *>, mouseEvent: Event?): JSONObject? {
        val key = model.schema.getPropertyName()
        when (val parentContainer = model.schema.parent?.extractProperty(currentData)
                ?: currentData) {
            null -> {
            }
            is JSONObject -> parentContainer.remove(key)
            is JSONArray -> parentContainer.put(key.toInt(), model.defaultValue)
            else -> throw IllegalStateException("Unknown parent container type: ${parentContainer::class.java}")
        }
        return currentData
    }
}