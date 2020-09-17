package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.ControlFactory
import com.github.hanseter.json.editor.IdReferenceProposalProvider
import com.github.hanseter.json.editor.ResolutionScopeProvider
import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.RegularSchemaWrapper
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.scene.control.Label
import org.everit.json.schema.Schema

abstract class TypeWithChildrenControl(schema: SchemaWrapper<*>) : TypeControl {
    override val node = FilterableTreeItem(TreeItemData(schema.title, null, Label(), null))
    protected abstract val children: List<TypeControl>

    protected fun createTypeControlsFromSchemas(
            contentSchemas: Collection<Schema>,
            refProvider: IdReferenceProposalProvider,
            resolutionScopeProvider: ResolutionScopeProvider
    ): List<TypeControl> {
        val controls = contentSchemas.map {
            ControlFactory.convert(
                    RegularSchemaWrapper(schema, it), refProvider, resolutionScopeProvider)
        }.sortedBy { it.schema.parent?.getPropertyOrder()?.indexOf(it.schema.getPropertyName()) }
        var orderedControls: List<TypeControl> = emptyList()
        val unorderedControls: List<TypeControl>
        val orderedPropertiesCount = controls.count {
            schema.getPropertyOrder().contains(it.schema.getPropertyName())
        }

        when {
            orderedPropertiesCount > 0 -> {
                orderedControls = controls.takeLast(orderedPropertiesCount)
                unorderedControls = controls.take(controls.size - orderedPropertiesCount).sortedBy {
                    it.schema.getPropertyName().toLowerCase()
                }
            }
            else -> unorderedControls = controls.sortedBy { it.schema.getPropertyName().toLowerCase() }
        }

        return orderedControls + unorderedControls
    }

    protected fun createValidityBinding() =
            children.map { it.valid }.fold(SimpleBooleanProperty(true) as ObservableBooleanValue) { a, b ->
                Bindings.and(a, b)
            }
}