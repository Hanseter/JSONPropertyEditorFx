package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.ControlFactory
import com.github.hanseter.json.editor.extensions.RegularSchemaWrapper
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.EditorContext
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import org.everit.json.schema.Schema

fun createTypeControlsFromSchemas(schema: SchemaWrapper<*>, contentSchemas: Collection<Schema>, context: EditorContext): List<TypeControl> {
    val controls = contentSchemas.map {
        ControlFactory.convert(RegularSchemaWrapper(schema, it), context)
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

fun createValidityBinding(children: List<TypeControl>) =
        children.fold(SimpleBooleanProperty(true) as ObservableBooleanValue) { a, b ->
            Bindings.and(a, b.valid)
        }

class TypeWithChildrenStatusControl(createLabel: String, onCreate: () -> Unit) : HBox() {

    private val label = Label().apply {
        styleClass += "type-with-children-label"
    }

    private val button = Button(createLabel).apply {
        onAction = EventHandler { onCreate() }
        managedProperty().bind(visibleProperty())
    }


    init {
        children.addAll(label, button)

        styleClass += "type-with-children-status-control"
    }

    fun displayNull() {
        label.text = TypeControl.NULL_PROMPT
        button.isVisible = true
    }

    fun displayNonNull(text: String) {
        label.text = text
        button.isVisible = false
    }

}