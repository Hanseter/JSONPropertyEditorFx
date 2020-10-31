package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.types.TupleModel
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import javafx.beans.value.ObservableBooleanValue
import org.json.JSONArray
import org.json.JSONObject

class TupleControl(override val model: TupleModel, context: EditorContext)
    : TypeControl {

    private val editorActionsContainer = context.createActionContainer(this)
    private val statusControl = TypeWithChildrenStatusControl("Create") { model.value = JSONArray() }
    override val node = FilterableTreeItem(TreeItemData(model.schema.title, model.schema.schema.description, statusControl, editorActionsContainer))
    private val children: List<TypeControl> = createTypeControlsFromSchemas(model.schema, model.contentSchemas, context)
    override val valid: ObservableBooleanValue = createValidityBinding(children)

    init {
        node.addAll(children.map { it.node })
    }

    override fun bindTo(type: BindableJsonType) {
        val subType = createSubArray(type, model.schema)
        if (subType != null) {
            if (node.isLeaf) {
                node.addAll(children.map { it.node })
            }
            children.forEach { it.bindTo(subType) }
        } else {
            node.clear()
        }
        model.bound = type
        boundValueChanged()
    }

    private fun boundValueChanged() {
        if (model.rawValue == JSONObject.NULL) {
            statusControl.displayNull()
        } else {
            statusControl.displayNonNull("[${children.size} Element${if (children.size == 1) "" else "s"}]")
        }
    }
}