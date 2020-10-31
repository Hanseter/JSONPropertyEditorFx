package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.actions.EditorAction
import com.github.hanseter.json.editor.controls.TypeControl
import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.RegularSchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.util.EditorContext
import com.github.hanseter.json.editor.util.RootBindableType
import javafx.beans.property.SimpleBooleanProperty
import org.everit.json.schema.Schema
import org.json.JSONObject

class JsonPropertiesPane(
        title: String,
        data: JSONObject,
        schema: Schema,
        private val refProvider: IdReferenceProposalProvider,
        private val resolutionScopeProvider: ResolutionScopeProvider,
        private val actions: List<EditorAction>,
        private val changeListener: (JSONObject, JsonPropertiesPane) -> Unit
) {
    val treeItem = FilterableTreeItem(TreeItemData(title, null, null, null, true))
    private val schema = RegularSchemaWrapper(null, schema, title)
    private var objectControl: TypeControl? = null
    private val contentHandler = ContentHandler(data)
    val valid = SimpleBooleanProperty(true)

    init {
        treeItem.isExpanded = false
        treeItem.expandedProperty().addListener { _, _, new ->
            if (new) {
                contentHandler.handleExpansion()
            }
        }
    }

    private fun initObjectControl() {
        val objectControl = ControlFactory.convert(schema, EditorContext(refProvider, resolutionScopeProvider, actions) { action, control ->
            val ret = action.apply(contentHandler.data, control.model)
            if (ret != null) {
                changeListener(ret, this)
                fillData(ret)
            }
        })
        valid.bind(objectControl.valid)
        if (objectControl.node.list.isEmpty()) treeItem.add(objectControl.node)
        else treeItem.addAll(objectControl.node.list)
        this.objectControl = objectControl
    }

    fun fillData(data: JSONObject) {
        contentHandler.updateData(data)
    }

    private fun fillSheet(data: JSONObject) {
        val type = RootBindableType(data)
        objectControl?.bindTo(type)
        type.registerListener {
            changeListener(type.value!!, this)
        }
    }

    private inner class ContentHandler(var data: JSONObject) {
        private var dataDirty = true

        fun handleExpansion() {
            if (objectControl == null) {
                initObjectControl()
            }
            if (dataDirty) {
                fillSheet(data)
                dataDirty = false
            }
        }

        fun updateData(data: JSONObject) {
            this.data = data
            if (treeItem.isExpanded) {
                fillSheet(data)
            } else {
                dataDirty = true
            }
        }
    }

}