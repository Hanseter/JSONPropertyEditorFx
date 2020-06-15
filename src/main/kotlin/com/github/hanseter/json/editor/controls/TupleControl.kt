package com.github.hanseter.json.editor.controls

import javafx.scene.layout.VBox
import org.everit.json.schema.ArraySchema
import org.everit.json.schema.Schema
import com.github.hanseter.json.editor.ControlFactory
import javafx.scene.control.TitledPane
import org.json.JSONObject
import org.json.JSONArray
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.BindableJsonArray
import com.github.hanseter.json.editor.IdReferenceProposalProvider
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import javafx.beans.value.ObservableBooleanValue

class TupleControl(
	override val schema: SchemaWrapper<ArraySchema>,
	private val contentSchemas: List<Schema>,
	refProvider: IdReferenceProposalProvider
) : TypeWithChildrenControl(schema) {

	override protected val children: List<TypeControl>
	override val valid: ObservableBooleanValue

	init {
		children = createTypeControlsFromSchemas(contentSchemas, refProvider)
		valid = createValidityBinding()
		node.content = VBox(*children.map { it.node }.toTypedArray())
	}

	override fun bindTo(type: BindableJsonType) {
		val subType = createSubArray(type)
		children.forEach { it.bindTo(subType) }
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