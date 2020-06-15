package com.github.hanseter.json.editor.controls

import org.everit.json.schema.ObjectSchema
import javafx.scene.control.TitledPane
import com.github.hanseter.json.editor.ControlFactory
import javafx.scene.layout.VBox
import org.json.JSONObject
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.BindableJsonObject
import com.github.hanseter.json.editor.IdReferenceProposalProvider
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import javafx.beans.value.ObservableBooleanValue

class ObjectControl(
	override val schema: SchemaWrapper<ObjectSchema>,
	refProvider: IdReferenceProposalProvider
) : TypeWithChildrenControl(schema) {

	private val requiredChildren: List<TypeControl>
	private val optionalChildren: List<TypeControl>
	override protected val children: List<TypeControl>
		get() = requiredChildren + optionalChildren
	override val valid: ObservableBooleanValue

	init {
		val childSchemas = schema.schema.getPropertySchemas().toMutableMap()
		requiredChildren = createTypeControlsFromSchemas(schema.schema.getRequiredProperties().map {
			childSchemas.remove(it)
		}.filterNotNull(), refProvider)
		optionalChildren = createTypeControlsFromSchemas(childSchemas.values, refProvider)
		valid = createValidityBinding()
		node.content = VBox(TitledPane("Required", VBox(*requiredChildren.map { it.node }.toTypedArray())),
			TitledPane("Optional", VBox(*optionalChildren.map { it.node }.toTypedArray()))
		)
	}

	fun bindChildrenToObject(json: BindableJsonType) {
		children.forEach {
			it.bindTo(json)
		}
	}

	override fun bindTo(type: BindableJsonType) {
		bindChildrenToObject(createSubType(type))
	}


	private fun createSubType(parent: BindableJsonType): BindableJsonObject {
		var obj = parent.getValue(schema) as? JSONObject
		if (obj == null) {
			obj = JSONObject()
			parent.setValue(schema, obj)
		}
		return BindableJsonObject(parent, obj)
	}
}