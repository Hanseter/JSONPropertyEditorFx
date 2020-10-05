package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.actions.EditorAction
import com.github.hanseter.json.editor.controls.TypeControl
import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.RegularSchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
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
) : FilterableTreeItem<TreeItemData>(TreeItemData(title, null, null, null, true)) {
    private val schema = RegularSchemaWrapper(null, schema, title)
    private var objectControl: TypeControl? = null
    private val contentHandler = ContentHandler(data)
    val valid = SimpleBooleanProperty(true)

    init {
        isExpanded = false
        expandedProperty().addListener { _, _, new ->
            if (new) {
                contentHandler.handleExpansion()
            }
        }
    }

    private fun initObjectControl() {
        val objectControl = ControlFactory.convert(schema, refProvider, resolutionScopeProvider, actions)
        valid.bind(objectControl.valid)
        if (objectControl.node.list.isEmpty()) add(objectControl.node)
        else addAll(objectControl.node.list)
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

    private inner class ContentHandler(private var data: JSONObject) {
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
            if (isExpanded) {
                fillSheet(data)
            } else {
                dataDirty = true
            }
        }
    }

}