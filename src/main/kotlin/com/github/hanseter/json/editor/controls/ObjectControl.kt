package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import com.github.hanseter.json.editor.types.PreviewString
import com.github.hanseter.json.editor.types.TypeModel
import com.github.hanseter.json.editor.util.LazyControl
import org.json.JSONObject

interface ObjectControl : TypeControl {
    override val model: TypeModel<JSONObject?, *>
    val requiredChildren: List<TypeControl>
    val optionalChildren: List<TypeControl>

    val allRequiredChildren: List<TypeControl>
    val allOptionalChildren: List<TypeControl>

    override val childControls: List<TypeControl>
        get() = requiredChildren + optionalChildren

    class LazyObjectControl(private val objectControl: ObjectControl) : LazyControl {
        override val control = TypeWithChildrenStatusControl(JsonPropertiesMl.bundle.getString("jsonEditor.control.object.create")) {
            objectControl.model.value = JSONObject()
        }.apply {
            isDisable = objectControl.model.schema.readOnly
        }

        override fun updateDisplayedValue() {
            updateLabel()
        }

        private fun updateLabel() {
            if (objectControl.model.rawValue == JSONObject.NULL || (objectControl.model.rawValue == null && objectControl.model.defaultValue == null)) {
                control.displayNull()
            } else {
                control.displayNonNull(objectControl.model.previewString.string)
            }
        }
    }
}