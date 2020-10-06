package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.actions.EditorAction
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox

class ActionsContainer(private val control: TypeControl, actions: List<EditorAction>) : HBox() {

    private val actionButtons: MutableMap<EditorAction, Button> = mutableMapOf()

    init {
        actions.forEach { addAction(it) }
    }

    fun addActionIfMatches(action: EditorAction, schema: SchemaWrapper<*>) {
        if (action.matches(schema)) {
            addAction(action)
        }
    }

    protected fun addAction(action: EditorAction) {
        val b = Button(action.text).apply {
            onAction = EventHandler {
                action.apply(control)
            }

            if (action.description.isNotBlank()) {
                tooltip = Tooltip(action.description)
            }
        }

        actionButtons[action] = b

        children.add(b)
    }

    fun updateDisablement() {
        actionButtons.forEach { (action, button) ->
            button.isDisable = action.shouldBeDisabled(control)
        }
    }


    companion object {
        @JvmStatic
        fun forActions(control: TypeControl, schema: SchemaWrapper<*>, actions: List<EditorAction>) =
                ActionsContainer(control, actions.filter { it.matches(schema) })
    }
}