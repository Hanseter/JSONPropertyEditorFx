package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.types.TupleModel
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import org.json.JSONArray
import org.json.JSONObject

class TupleControl(override val model: TupleModel, context: EditorContext)
    : TypeControl {

    override val control = TypeWithChildrenStatusControl("Create") { model.value = JSONArray() }

    private val childControlsNotNull: List<TypeControl> = createTypeControlsFromSchemas(model.schema, model.contentSchemas, context)
    override var childControls: List<TypeControl> = createTypeControlsFromSchemas(model.schema, model.contentSchemas, context)


    override fun bindTo(type: BindableJsonType) {
        val subType = createSubArray(type, model.schema)
        if (subType != null) {
            if (childControls.isEmpty()) {
                childControls = childControlsNotNull
            }
            childControls.forEach { it.bindTo(subType) }
        } else {
            childControls = emptyList()
        }
        model.bound = type
        boundValueChanged()
    }

    private fun boundValueChanged() {
        if (model.rawValue == JSONObject.NULL) {
            control.displayNull()
        } else {
            control.displayNonNull("[${childControls.size} Element${if (childControls.size == 1) "" else "s"}]")
        }
    }
}