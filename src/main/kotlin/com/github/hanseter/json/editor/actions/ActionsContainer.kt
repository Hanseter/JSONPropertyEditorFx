package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.controls.TypeControl
import javafx.event.Event
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import javafx.scene.layout.Region

class ActionsContainer(
    private val control: TypeControl,
    actions: List<EditorAction>,
    private val objId: String,
    private val executeActionCallback: (Event, EditorAction, TypeControl) -> Unit
) : HBox() {

    private val actionButtons: List<Pair<EditorAction, Button>>

    init {
        actionButtons = actions.filter { it.selector.matches(control.model) }.map {
            it to createButton(it, executeActionCallback)
        }
        children.addAll(actionButtons.map { it.second })
    }

    private fun createButton(
        action: EditorAction,
        executeActionCallback: (Event, EditorAction, TypeControl) -> Unit
    ) = Button("", action.createIcon(16)).apply {
        prefWidth = 24.0
        prefHeight = 24.0
        minWidth = USE_PREF_SIZE
        minHeight = USE_PREF_SIZE
        maxWidth = USE_PREF_SIZE
        maxHeight = USE_PREF_SIZE
        onAction = EventHandler {
            executeActionCallback(it, action, control)
        }

        if (action.description.isNotBlank()) {
            tooltip = Tooltip(action.description)
        }
    }


    fun updateDisablement() {
        actionButtons.forEach { (action, button) ->
            button.isDisable = action.shouldBeDisabled(control.model, objId)
        }
    }
}