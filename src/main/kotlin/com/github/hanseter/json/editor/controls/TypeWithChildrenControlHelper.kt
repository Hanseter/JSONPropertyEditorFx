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

fun createTypeControlsFromSchemas(schema: EffectiveSchema<*>,
                                  contentSchemas: Collection<Schema>,
                                  context: EditorContext): List<TypeControl> =
        contentSchemas.map {
            ControlFactory.convert(SimpleEffectiveSchema(schema, it), context)
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