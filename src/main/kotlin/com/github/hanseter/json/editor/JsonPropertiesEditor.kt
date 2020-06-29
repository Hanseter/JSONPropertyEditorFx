package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.schemaExtensions.ColorFormat
import com.github.hanseter.json.editor.schemaExtensions.IdReferenceFormat
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import org.everit.json.schema.ObjectSchema
import org.everit.json.schema.Schema
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import java.net.URI
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.binding.Bindings
import javafx.beans.value.ObservableBooleanValue
import javafx.scene.control.ScrollPane

class JsonPropertiesEditor(
		private val referenceProposalProvider: IdReferenceProposalProvider = IdReferenceProposalProvider.IdReferenceProposalProviderEmpty,
		private val readOnly: Boolean = false,
		private val numberOfInitiallyOpenedObjects: Int = 5
) :
	VBox() {
	private val idsToPanes = mutableMapOf<String, JsonPropertiesPane>()
	private val scrollPane = ScrollPane()
	private val filterText = TextField()
	private val paneContainer = VBox()
	private val valid_ = SimpleBooleanProperty(true)
	val valid: ReadOnlyBooleanProperty
		get() = valid_


	init {
		filterText.promptText = "Filter properties";
		filterText.textProperty().addListener { _, _, new -> idsToPanes.values.forEach { it.setPropertyFilter(new) } }
		scrollPane.content = paneContainer
		scrollPane.isFitToWidth = true
		this.children.addAll(filterText, scrollPane)
	}

	@JvmOverloads
	fun display(
		objId: String,
		title: String,
		obj: JSONObject,
		schema: JSONObject,
		resolutionScope: URI? = null,
		callback: (JSONObject) -> JSONObject
	) {
		if (idsToPanes.contains(objId)) {
			updateObject(objId, obj)
			return
		}
		val pane =
			createTitledPaneForSchema(title, obj, parseSchema(schema, resolutionScope), filterText.text, callback)
		pane.fillData(obj)
		idsToPanes[objId] = pane
		paneContainer.children.add(pane)
		if (idsToPanes.size <= numberOfInitiallyOpenedObjects) {
			pane.isExpanded = true
		}
		rebindValidProperty()
	}

	private fun parseSchema(schema: JSONObject, resolutionScope: URI?): ObjectSchema {
		val ret: Schema
		val slb = SchemaLoader.builder()
			.useDefaults(true)
			.addFormatValidator(ColorFormat.Validator)
			.addFormatValidator(IdReferenceFormat.Validator)
			.schemaJson(schema)

		if (resolutionScope != null) {
			slb.resolutionScope(resolutionScope)
		}

		ret = slb.build().load().readOnly(readOnly).build()

		if (ret is ObjectSchema) {
			return ret
		}
		throw IllegalArgumentException("Only valid object schemas can be displayed but the provided schema is $ret")
	}

	fun updateObject(
		objId: String,
		obj: JSONObject
	) {
		val titledPane = idsToPanes[objId] ?: return
		titledPane.fillData(obj)
	}

	fun removeObject(objId: String) {
		children.remove(idsToPanes.remove(objId))
		rebindValidProperty()
	}

	fun clear() {
		children.clear()
		idsToPanes.clear()
		filterText.clear()
		rebindValidProperty()
	}


	private fun rebindValidProperty() {
		if (idsToPanes.isEmpty()) {
			valid_.unbind()
			valid_.set(true)
		} else {
			valid_.bind(idsToPanes.values.map { it.valid as ObservableBooleanValue }.reduce { a, b -> Bindings.and(a, b) })
		}
	}

	private fun createTitledPaneForSchema(
		title: String,
		data: JSONObject,
		schema: ObjectSchema,
		filter: String,
		callback: (JSONObject) -> JSONObject
	): JsonPropertiesPane =
		JsonPropertiesPane(title, data, schema, filter, referenceProposalProvider) { obj, pane ->
			pane.fillData(callback(obj))
		}
}