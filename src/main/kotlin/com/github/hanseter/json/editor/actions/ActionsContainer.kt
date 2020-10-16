package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.controls.TypeControl
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox

class ActionsContainer(private val control: TypeControl, private val actions: List<EditorAction>,
                       private val executeActionCallback: (EditorAction, TypeControl) -> Unit)
    : HBox() {

    constructor(original: ActionsContainer, additionalActions: List<EditorAction>) :
            this(original.control, original.actions + additionalActions,
                    original.executeActionCallback)

    private val actionButtons: List<Pair<EditorAction, Button>>

    init {
        actionButtons = actions.filter { it.matches(control.schema) }.map {
            it to createButton(it, executeActionCallback)
        }
        children.addAll(actionButtons.map { it.second })
    }

    private fun createButton(action: EditorAction, executeActionCallback: (EditorAction, TypeControl) -> Unit) = Button(action.text).apply {
        onAction = EventHandler {
            executeActionCallback(action, control)
        }
        if (action.description.isNotBlank()) {
            tooltip = Tooltip(action.description)
        }
    }


    fun updateDisablement() {
        actionButtons.forEach { (action, button) ->
            button.isDisable = action.shouldBeDisabled(control.schema)
        }
    }
}