package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.extensions.NullableEffectiveSchema
import com.github.hanseter.json.editor.types.TypeModel
import javafx.event.Event
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONPointer

object ResetToNullAction : EditorAction {
    override val text: String = "Ø"
    override val description: String = "Reset to null"
    override val selector: TargetSelector = TargetSelector.AllOf(listOf(
            TargetSelector.ReadOnly.invert(),
            TargetSelector { it.schema is NullableEffectiveSchema }
    ))

    override fun apply(currentData: JSONObject, model: TypeModel<*, *>, mouseEvent: Event?): JSONObject {
        val key = model.schema.propertyName
        when (val parentContainer = JSONPointer(model.schema.pointer.dropLast(1)).queryFrom(currentData)) {
            is JSONObject -> parentContainer.put(key, JSONObject.NULL)
            is JSONArray -> parentContainer.put(key.toInt(), JSONObject.NULL)
            else -> throw IllegalStateException("Unknown parent container type: ${parentContainer::class.java}")
        }
        return currentData
    }

}