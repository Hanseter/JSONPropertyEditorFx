package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.types.TypeModel
import org.json.JSONArray
import org.json.JSONObject

object ResetToNullAction : EditorAction {
    override val text: String = "Ø"
    override val description: String = "Reset to null"
    override val selector: ActionTargetSelector = ActionTargetSelector.AllOf(listOf(
            ActionTargetSelector.Required().invert(),
            ActionTargetSelector.ReadOnly().invert()
    ))

    override fun apply(currentData: JSONObject, model: TypeModel<*>): JSONObject {
        val key = model.schema.getPropertyName()
        when (val parentContainer = model.schema.parent?.extractProperty(currentData)
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