package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.types.CombinedObjectModel
import com.github.hanseter.json.editor.util.BindableJsonType
import org.json.JSONObject

class CombinedObjectControl(override val model: CombinedObjectModel, val controls: List<ObjectControl>)
    : ObjectControl {

    private val requiredChildrenNotNull = controls.flatMap { it.requiredChildren }.distinctBy { it.model.schema.title }
    private val optionalChildrenNotNull = controls.flatMap { it.optionalChildren }.distinctBy { it.model.schema.title }

    override var requiredChildren: List<TypeControl> = emptyList()
    override var optionalChildren: List<TypeControl> = emptyList()

    override val control = TypeWithChildrenStatusControl("Create") {
        model.value = JSONObject()
    }

    override fun bindTo(type: BindableJsonType) {

        controls.forEach { it.bindTo(type) }
        model.bound = type
/*
        if (model.rawValue == null && isRequiredSchema(model.schema)) {
            model.value = JSONObject()
        }
*/
        valueChanged()


    }

    private fun valueChanged() {
        if (model.value != null) {
            if (requiredChildren.isEmpty() && optionalChildren.isEmpty()) {
                requiredChildren = requiredChildrenNotNull
                optionalChildren = optionalChildrenNotNull
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