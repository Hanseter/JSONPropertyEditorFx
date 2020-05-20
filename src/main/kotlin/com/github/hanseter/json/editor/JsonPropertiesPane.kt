package com.github.hanseter.json.editor

import javafx.scene.control.TitledPane
import org.json.JSONObject
import javafx.scene.layout.VBox
import javafx.beans.value.ChangeListener
import org.everit.json.schema.Schema
import org.everit.json.schema.ObjectSchema
import com.github.hanseter.json.editor.controls.ObjectControl
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.BindableJsonObject
import com.github.hanseter.json.editor.extensions.SchemaWrapper

class JsonPropertiesPane(
	title: String,
	data: JSONObject,
	schema: ObjectSchema,
	filter: String,
	private val refProvider: IdReferenceProposalProvider,
	private val changeListener: (JSONObject, JsonPropertiesPane) -> Unit
) : TitledPane(title, null) {
	private val schema = SchemaWrapper<ObjectSchema>(null, schema, title)
	private var objectControl: ObjectControl? = null
	private val contentHandler = ContentHandler(data, filter)

	init {
		setExpanded(false)
		expandedProperty().addListener { _, _, new ->
			if (new) {
				contentHandler.handleExpansion()
			}
		}
	}

	private fun initObjectControl() {
		objectControl = ObjectControl(schema, refProvider)
		setContent(objectControl?.node?.getContent())
	}

	fun setPropertyFilter(filter: String) {
		contentHandler.updateFilter(filter)
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

	private fun filterSheet(filter: String) {
		objectControl?.applyFilter(filter, "")
	}

	private inner class ContentHandler(private var data: JSONObject, private var filterString: String) {
		private var dataDirty = true
		private var filterDirty = true

		fun handleExpansion() {
			if (objectControl == null) {
				initObjectControl()
			}
			if (dataDirty) {
				fillSheet(data)
				dataDirty = false
			}
			if (filterDirty) {
				filterSheet(filterString)
				filterDirty = false
			}
		}

		fun updateData(data: JSONObject) {
			this.data = data
			if (isExpanded()) {
				fillSheet(data)
			} else {
				dataDirty = true
			}
		}

		fun updateFilter(filter: String) {
			this.filterString = filter
			if (isExpanded()) {
				filterSheet(filterString)
			} else {
				dataDirty = true
			}
		}
	}

}