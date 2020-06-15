package com.github.hanseter.json.editor.controls

import org.everit.json.schema.Schema
import javafx.scene.layout.VBox
import com.github.hanseter.json.editor.ControlFactory
import javafx.scene.control.TitledPane
import com.github.hanseter.json.editor.IdReferenceProposalProvider.IdReferenceProposalProviderEmpty
import com.github.hanseter.json.editor.IdReferenceProposalProvider
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.binding.Bindings
import javafx.beans.value.ObservableBooleanValue

abstract class TypeWithChildrenControl(schema: SchemaWrapper<*>) : TypeControl {
	override val node = TitledPane(schema.title, null)
	abstract protected val children: List<TypeControl>

	protected fun createTypeControlsFromSchemas(
		contentSchemas: Collection<Schema>,
		refProvider: IdReferenceProposalProvider
	) = contentSchemas.map { ControlFactory.convert(SchemaWrapper(schema, it), refProvider) }
		.sortedBy { it.schema.getPropertyName().toLowerCase() }

	override fun applyFilter(filterString: String, parentAttributeDisplayName: String) {
		val qualifiedAttributeDisplayName =
			createQualifiedAttributeName(parentAttributeDisplayName, node.text)
		if (attributeMatchesFilter(filterString, node.text, qualifiedAttributeDisplayName)) {
			children.forEach { it.applyFilter("", qualifiedAttributeDisplayName) }
			node.setManaged(true)
			node.setVisible(true)
			return
		}

		children.forEach { it.applyFilter(filterString, qualifiedAttributeDisplayName) }
		val anyChildVisible = children.any { it.node.isVisible() }
		node.setManaged(anyChildVisible)
		node.setVisible(anyChildVisible)
	}

	override fun matchesFilter(filterString: String, parentAttributeDisplayName: String): Boolean {
		val qualifiedAttributeDisplayName =
			createQualifiedAttributeName(parentAttributeDisplayName, node.text)
		if (attributeMatchesFilter(filterString, node.text, qualifiedAttributeDisplayName)) {
			return true
		}
		return children.any { it.matchesFilter(filterString, qualifiedAttributeDisplayName) }
	}

	protected fun createValidityBinding() =
		children.map { it.valid }.fold(SimpleBooleanProperty(true) as ObservableBooleanValue) { a, b ->
			Bindings.and(
				a,
				b
			)
		}

}