package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.ControlFactory
import com.github.hanseter.json.editor.IdReferenceProposalProvider
import com.github.hanseter.json.editor.ResolutionScopeProvider
import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.scene.control.Label
import org.everit.json.schema.Schema

abstract class TypeWithChildrenControl(schema: SchemaWrapper<*>) : TypeControl {
	override val node = FilterableTreeItem(TreeItemData(schema.title,null, Label(),null))
	protected abstract val children: List<TypeControl>

	protected fun createTypeControlsFromSchemas(
		contentSchemas: Collection<Schema>,
		refProvider: IdReferenceProposalProvider,
		resolutionScopeProvider: ResolutionScopeProvider
	) = contentSchemas.map { ControlFactory.convert(SchemaWrapper(schema, it), refProvider, resolutionScopeProvider) }
		.sortedBy { it.schema.getPropertyName().toLowerCase() }

	protected fun createValidityBinding() =
		children.map { it.valid }.fold(SimpleBooleanProperty(true) as ObservableBooleanValue) { a, b ->
			Bindings.and(a, b)
		}
}