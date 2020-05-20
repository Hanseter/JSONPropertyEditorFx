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

class ObjectControl(
	override val schema: SchemaWrapper<ObjectSchema>,
	refProvider: IdReferenceProposalProvider
) :
	TypeWithChildrenControl(schema, schema.schema.getPropertySchemas().values, refProvider) {


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