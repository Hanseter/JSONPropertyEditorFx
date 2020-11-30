package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.types.TypeModel
import javafx.event.Event
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONPointer

object ResetToDefaultAction : EditorAction {
    override val text: String = "↻"
    override val description: String = "Reset to default"
    override val selector: TargetSelector = TargetSelector.AllOf(listOf(
            TargetSelector.ReadOnly.invert(),
            TargetSelector.AnyOf(listOf(
                    TargetSelector.Required.invert(),
                    TargetSelector { it.schema.defaultValue != null }
            ))
    ))

    override fun apply(currentData: JSONObject, model: TypeModel<*, *>, mouseEvent: Event?): JSONObject? {
        val key = model.schema.propertyName
        when (val parentContainer = JSONPointer(model.schema.pointer.dropLast(1)).queryFrom(currentData)) {
            is JSONObject -> parentContainer.remove(key)
            is JSONArray -> parentContainer.put(key.toInt(), model.defaultValue ?: JSONObject.NULL)
            else -> throw IllegalStateException("Unknown parent container type: ${parentContainer::class.java}")
        }
        return currentData
    }
}