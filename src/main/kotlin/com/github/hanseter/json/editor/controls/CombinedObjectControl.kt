package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.types.CombinedObjectModel
import com.github.hanseter.json.editor.util.BindableJsonType
import org.json.JSONObject

class CombinedObjectControl(override val model: CombinedObjectModel, val controls: List<ObjectControl>)
    : ObjectControl {

    override val allRequiredChildren = controls.flatMap { it.allRequiredChildren }.distinctBy { it.model.schema.title }
    override val allOptionalChildren = controls.flatMap { it.allOptionalChildren }.distinctBy { it.model.schema.title }

    override var requiredChildren: List<TypeControl> = allRequiredChildren
    override var optionalChildren: List<TypeControl> = allOptionalChildren

    override val control = TypeWithChildrenStatusControl("Create") {
        model.value = JSONObject()
    }

    override fun bindTo(type: BindableJsonType) {
        model.bound = type
        if (model.rawValue == null && model.schema.required) {
            model.value = JSONObject()
        }

        controls.forEach { it.bindTo(type) }

        valueChanged()
    }

    private fun valueChanged() {
        if (model.value != null) {
            if (requiredChildren.isEmpty() && optionalChildren.isEmpty()) {
                requiredChildren = allRequiredChildren
                optionalChildren = allOptionalChildren
            }

        } else {
            requiredChildren = emptyList()
            optionalChildren = emptyList()
        }

        if (model.rawValue == JSONObject.NULL || model.rawValue == null) {
            control.displayNull()
        } else {
            control.displayNonNull("")
        }
    }

}