package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.PropertiesEditInput
import com.github.hanseter.json.editor.PropertiesEditResult
import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import com.github.hanseter.json.editor.types.TypeModel
import javafx.event.Event
import javafx.scene.Node
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONPointer

object ResetToDefaultAction : EditorAction {
    override val description: String = JsonPropertiesMl.bundle.getString("jsonEditor.actions.resetToDefault")
    override val priority: Int
        get() = 5_000
    override val selector: TargetSelector = TargetSelector.AllOf(listOf(
            TargetSelector.ReadOnly.invert(),
            TargetSelector.AnyOf(listOf(
                    TargetSelector.Required.invert(),
                    TargetSelector { it.schema.defaultValue != null }
            ))
    ))

    override fun createIcon(size: Int): Node = EditorAction.createTextIcon( "↻", size)

    override fun apply(input: PropertiesEditInput, model: TypeModel<*, *>, mouseEvent: Event?): PropertiesEditResult {
        val key = model.schema.propertyName
        when (val parentContainer = JSONPointer(model.schema.pointer.dropLast(1)).queryFrom(input.data)) {
            is JSONObject -> parentContainer.remove(key)
            is JSONArray -> parentContainer.put(key.toInt(), model.defaultValue ?: JSONObject.NULL)
            else -> throw IllegalStateException("Unknown parent container type: ${parentContainer::class.java}")
        }
         return PropertiesEditResult(model.bound?.rootType?.getValue() ?: input.data)
    }
}