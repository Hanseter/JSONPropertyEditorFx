package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.IdReferenceProposalProvider
import com.github.hanseter.json.editor.ResolutionScopeProvider
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonArray
import com.github.hanseter.json.editor.util.BindableJsonType
import javafx.beans.value.ObservableBooleanValue
import org.everit.json.schema.ArraySchema
import org.everit.json.schema.Schema
import org.json.JSONArray

class TupleControl(
	override val schema: SchemaWrapper<ArraySchema>,
	contentSchemas: List<Schema>,
	refProvider: IdReferenceProposalProvider,
	resolutionScopeProvider: ResolutionScopeProvider
) : TypeWithChildrenControl(schema) {

	private val children: List<TypeControl> = createTypeControlsFromSchemas(contentSchemas, refProvider, resolutionScopeProvider)
	override val valid: ObservableBooleanValue = createValidityBinding(children)

	init {
		node.addAll(children.map { it.node })
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