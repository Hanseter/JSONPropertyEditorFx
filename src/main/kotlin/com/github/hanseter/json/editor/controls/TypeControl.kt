package com.github.hanseter.json.editor.controls

import org.everit.json.schema.Schema
import javafx.scene.Node
import org.json.JSONObject
import org.json.JSONArray
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.value.ObservableBooleanValue

interface TypeControl {
	val schema: SchemaWrapper<*>
	val node: Node
	val valid: ObservableBooleanValue
	
	/**
	 * Applies the filter to the node of this control. I.e. it checks whether the filter matches and if it doesn't makes this node invisible and unmanaged.
	 **/
	fun applyFilter(filterString: String, parentAttributeDisplayName: String) {
		val matches = matchesFilter(filterString, parentAttributeDisplayName)
		node.setManaged(matches)
		node.setVisible(matches)
	}

	/**
	 * Checks whether the supplied filter matches this control.
	 **/
	fun matchesFilter(filterString: String, parentAttributeDisplayName: String): Boolean

	fun attributeMatchesFilter(
		filterString: String,
		simpleAttributeName: String,
		qualifiedAttributName: String
	): Boolean = when {
		simpleAttributeName.startsWith(filterString) -> true
		qualifiedAttributName.startsWith(filterString) -> true
		else -> false
	}

	fun createQualifiedAttributeName(parentQualifiedAttributeName: String, simpleAttributeName: String) =
		parentQualifiedAttributeName + simpleAttributeName + ATTRIBUTE_NAME_SEPERATOR

	fun bindTo(type: BindableJsonType)

	companion object {
		const val ATTRIBUTE_NAME_SEPERATOR = "/"
	}

}