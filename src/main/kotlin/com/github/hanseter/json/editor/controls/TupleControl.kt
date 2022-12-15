package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import com.github.hanseter.json.editor.types.PreviewString
import com.github.hanseter.json.editor.types.TupleModel
import com.github.hanseter.json.editor.util.BindableJsonArrayEntry
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import com.github.hanseter.json.editor.util.LazyControl
import org.json.JSONArray
import org.json.JSONObject

class TupleControl(override val model: TupleModel, context: EditorContext) : TypeControl {

    private val childControlsNotNull: List<TypeControl> =
        createTypeControlsFromSchemas(model.schema, model.contentSchemas, context)
    override val childControls: MutableList<TypeControl> = childControlsNotNull.toMutableList()


    override fun bindTo(type: BindableJsonType) {
        val subType = createSubArray(type, model.schema, model)
        if (subType != null) {
            if (childControls.isEmpty()) {
                childControls.addAll(childControlsNotNull)
            }
            childControls.forEachIndexed { index, it ->
                it.bindTo(BindableJsonArrayEntry(subType, index))
            }
        } else {
            childControls.clear()
        }
        model.bound = type
    }

    override fun createLazyControl(): LazyControl = TupleLazyControl()

    private inner class TupleLazyControl : LazyControl {
        override val control =
            TypeWithChildrenStatusControl("Create") { model.value = JSONArray() }.apply {
                isDisable = model.schema.readOnly
            }

        override fun updateDisplayedValue() {
            if (model.rawValue == JSONObject.NULL || (model.rawValue == null && model.defaultValue == null)) {
                control.displayNull()
            } else {
                control.displayNonNull("[${childControls.size} Element${if (childControls.size == 1) "" else "s"}]")
            }
        }
    }

}