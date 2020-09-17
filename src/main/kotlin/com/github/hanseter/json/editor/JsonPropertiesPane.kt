package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.controls.ObjectControl
import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.RegularSchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.util.BindableJsonObject
import javafx.beans.property.SimpleBooleanProperty
import org.everit.json.schema.ObjectSchema
import org.json.JSONObject

class JsonPropertiesPane(
        title: String,
        data: JSONObject,
        schema: ObjectSchema,
        private val refProvider: IdReferenceProposalProvider,
        private val resolutionScopeProvider: ResolutionScopeProvider,
        private val changeListener: (JSONObject, JsonPropertiesPane) -> Unit
) : FilterableTreeItem<TreeItemData>(TreeItemData(title, null, null, null, true)) {
    private val schema = RegularSchemaWrapper(null, schema, title)
    private var objectControl: ObjectControl? = null
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
        objectControl = ObjectControl(schema, refProvider, resolutionScopeProvider)
        valid.bind(objectControl?.valid)
        objectControl?.node?.list?.let { addAll(it) }
    }

    fun fillData(data: JSONObject) {
        contentHandler.updateData(data)
    }

    private fun fillSheet(data: JSONObject) {
        val type = BindableJsonObject(null, data)
        objectControl?.bindChildrenToObject(type)
        type.registerListener {
            changeListener(type.obj, this)
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