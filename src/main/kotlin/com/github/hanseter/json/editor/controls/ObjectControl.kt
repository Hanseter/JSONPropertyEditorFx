package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.IdReferenceProposalProvider
import com.github.hanseter.json.editor.ResolutionScopeProvider
import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.util.BindableJsonObject
import com.github.hanseter.json.editor.util.BindableJsonType
import javafx.beans.value.ObservableBooleanValue
import org.everit.json.schema.ObjectSchema
import org.json.JSONObject

class ObjectControl(
	override val schema: SchemaWrapper<ObjectSchema>,
	refProvider: IdReferenceProposalProvider,
	resolutionScopeProvider: ResolutionScopeProvider
) : TypeWithChildrenControl(schema) {

	private val requiredChildren: List<TypeControl>
	private val optionalChildren: List<TypeControl>
	override val children: List<TypeControl>
		get() = requiredChildren + optionalChildren
	override val valid: ObservableBooleanValue

	init {
		val childSchemas = schema.schema.propertySchemas.toMutableMap()
		requiredChildren = createTypeControlsFromSchemas(schema.schema.requiredProperties.mapNotNull {
			childSchemas.remove(it)
		}, refProvider, resolutionScopeProvider)
		optionalChildren = createTypeControlsFromSchemas(childSchemas.values, refProvider, resolutionScopeProvider)
		valid = createValidityBinding()

		if(requiredChildren.isNotEmpty()) {
			node.add(FilterableTreeItem(TreeItemData("Required", null, null, null, isRoot = false, isHeadline = true)))
			node.addAll(requiredChildren.map { it.node })
		}

		if (optionalChildren.isNotEmpty()) {
			node.add(FilterableTreeItem(TreeItemData("Optional", null, null, null, isRoot = false, isHeadline = true)))
			node.addAll(optionalChildren.map { it.node })
		}
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