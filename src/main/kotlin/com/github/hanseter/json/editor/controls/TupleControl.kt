package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.util.BindableJsonArray
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import javafx.beans.value.ObservableBooleanValue
import org.everit.json.schema.ArraySchema
import org.everit.json.schema.Schema
import org.json.JSONArray
import org.json.JSONObject

class TupleControl(override val schema: SchemaWrapper<ArraySchema>, contentSchemas: List<Schema>, context: EditorContext)
    : TypeControl {

    private val editorActionsContainer = context.createActionContainer(this)
    private val statusControl = TypeWithChildrenStatusControl("Create") { bound?.setValue(schema, JSONArray()) }
    override val node = FilterableTreeItem(TreeItemData(schema.title, null, statusControl, editorActionsContainer))
    private var bound: BindableJsonType? = null
    private val children: List<TypeControl> = createTypeControlsFromSchemas(schema, contentSchemas, context)
    override val valid: ObservableBooleanValue = createValidityBinding(children)

    init {
        node.addAll(children.map { it.node })
    }

    override fun bindTo(type: BindableJsonType) {
        val subType = createSubArray(type)
        if (subType != null) {
            if (node.isLeaf) {
                node.addAll(children.map { it.node })
            }
            children.forEach { it.bindTo(subType) }
        } else {
            node.clear()
        }
        bound = type
        boundValueChanged()
    }

    private fun boundValueChanged() {
        if (bound?.getValue(schema) == JSONObject.NULL) {
            statusControl.displayNull()
        } else {
            statusControl.displayNonNull("[${children.size} Element${if (children.size == 1) "" else "s"}]")
        }
    }

    private fun createSubArray(parent: BindableJsonType): BindableJsonArray? {
        val rawArr = parent.getValue(schema)

        if (rawArr == JSONObject.NULL) {
            return null
        }

        var arr = rawArr as? JSONArray
        if (arr == null) {
            arr = JSONArray()
            parent.setValue(schema, arr)
        }
        return BindableJsonArray(parent, arr)
    }
}