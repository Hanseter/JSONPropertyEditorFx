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

class TupleControl(override val schema: SchemaWrapper<ArraySchema>, contentSchemas: List<Schema>, context: EditorContext)
    : TypeControl {

    private val editorActionsContainer = context.createActionContainer(this)
    override val node = FilterableTreeItem(TreeItemData(schema.title, null, null, editorActionsContainer))
    private var bound: BindableJsonType? = null
    private val children: List<TypeControl> = createTypeControlsFromSchemas(schema, contentSchemas, context)
    override val valid: ObservableBooleanValue = createValidityBinding(children)

    init {
        node.addAll(children.map { it.node })
    }

    override fun bindTo(type: BindableJsonType) {
        val subType = createSubArray(type)
        children.forEach { it.bindTo(subType) }
        bound = type
    }

    private fun createSubArray(parent: BindableJsonType): BindableJsonArray {
        var arr = parent.getValue(schema) as? JSONArray
        if (arr == null) {
            arr = JSONArray()
            parent.setValue(schema, arr)
        }
        return BindableJsonArray(parent, arr)
    }
}