package com.github.hanseter.json.editor.controls

import org.everit.json.schema.Schema
import javafx.scene.layout.VBox
import com.github.hanseter.json.editor.ControlFactory
import javafx.scene.control.TitledPane
import com.github.hanseter.json.editor.IdReferenceProposalProvider.IdReferenceProposalProviderEmpty
import com.github.hanseter.json.editor.IdReferenceProposalProvider
import com.github.hanseter.json.editor.extensions.SchemaWrapper

abstract class TypeWithChildrenControl(
	schema: SchemaWrapper<*>,
	childSchemas: Collection<Schema>,
	refProvider: IdReferenceProposalProvider
) :
	TypeControl {
	override val node: TitledPane
	protected val content = VBox()
	protected val children: MutableList<TypeControl>

	init {
		childSchemas.map { it.isReadOnly() }
		children = childSchemas.map { ControlFactory.convert(SchemaWrapper(schema, it), refProvider) }.toMutableList()
		content.getChildren().setAll(children.map { it.node })
		node = TitledPane(schema.title, content)
	}

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

}