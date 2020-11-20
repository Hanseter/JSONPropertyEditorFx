package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.ControlFactory
import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.extensions.SimpleEffectiveSchema
import com.github.hanseter.json.editor.util.EditorContext
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import org.everit.json.schema.Schema

fun createTypeControlsFromSchemas(schema: EffectiveSchema<*>, contentSchemas: Collection<Schema>, context: EditorContext): List<TypeControl> {
    val controls = contentSchemas.map {
        ControlFactory.convert(SimpleEffectiveSchema(schema, it), context)
    }.sortedBy { it.model.schema.parent?.getPropertyOrder()?.indexOf(it.model.schema.getPropertyName()) }
    var orderedControls: List<TypeControl> = emptyList()
    val unorderedControls: List<TypeControl>
    val orderedPropertiesCount = controls.count {
        schema.getPropertyOrder().contains(it.model.schema.getPropertyName())
    }

    when {
        orderedPropertiesCount > 0 -> {
            orderedControls = controls.takeLast(orderedPropertiesCount)
            unorderedControls = controls.take(controls.size - orderedPropertiesCount).sortedBy {
                it.model.schema.getPropertyName().toLowerCase()
            }
        }
        else -> unorderedControls = controls.sortedBy { it.model.schema.title.toLowerCase() }
    }

    return orderedControls + unorderedControls
}

class TypeWithChildrenStatusControl(createLabel: String, onCreate: () -> Unit) : HBox() {

    private val label = Label().apply {
        styleClass += "type-with-children-label"
    }

    val button = Button(createLabel).apply {
        onAction = EventHandler { onCreate() }
        managedProperty().bind(visibleProperty())
    }


    init {
        children.addAll(label, button)

        styleClass += "type-with-children-status-control"
    }

    fun getDecorationsAnchor(): Control = label

    fun displayNull() {
        label.text = TypeControl.NULL_PROMPT
        button.isVisible = true
    }

    fun displayNonNull(text: String) {
        label.text = text
        button.isVisible = false
    }

}