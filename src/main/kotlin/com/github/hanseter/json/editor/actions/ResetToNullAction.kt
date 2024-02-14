package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.PropertiesEditInput
import com.github.hanseter.json.editor.PropertiesEditResult
import com.github.hanseter.json.editor.extensions.PartialEffectiveSchema
import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import com.github.hanseter.json.editor.types.TypeModel
import javafx.event.Event
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONPointer

object ResetToNullAction : EditorAction {
    override val text: String = "Ø"
    override val description: String = JsonPropertiesMl.bundle.getString("jsonEditor.actions.resetToNull")
    override val selector: TargetSelector = TargetSelector.AllOf(listOf(
            TargetSelector.ReadOnly.invert(),
            TargetSelector { (it.schema as? PartialEffectiveSchema)?.allowsNull == true }
    ))

    override fun apply(input: PropertiesEditInput, model: TypeModel<*, *>, mouseEvent: Event?): PropertiesEditResult {
        val key = model.schema.propertyName
        when (val parentContainer = JSONPointer(model.schema.pointer.dropLast(1)).queryFrom(input.data)) {
            is JSONObject -> parentContainer.put(key, JSONObject.NULL)
            is JSONArray -> parentContainer.put(key.toInt(), JSONObject.NULL)
            else -> throw IllegalStateException("Unknown parent container type: ${parentContainer::class.java}")
        }
        return PropertiesEditResult(model.bound?.rootType?.getValue() ?: input.data)
    }

}