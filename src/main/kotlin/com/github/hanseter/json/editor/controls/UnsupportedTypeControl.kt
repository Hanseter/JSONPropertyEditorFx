package com.github.hanseter.json.editor.controls

import org.everit.json.schema.Schema
import javafx.scene.control.Label
import org.json.JSONObject
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.extensions.SchemaWrapper

class UnsupportedTypeControl(override val schema: SchemaWrapper<*>) : TypeControl {
	override fun matchesFilter(filterString: String, parentAttributeDisplayName: String): Boolean = true
	override val node =
		Label("Schema ${schema.schema.schemaLocation} with type ${schema.schema::class.java.name} cannot be displayed.")

	override fun bindTo(type: BindableJsonType) {}
}