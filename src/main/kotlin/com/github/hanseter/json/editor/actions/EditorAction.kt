package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.PropertiesEditInput
import com.github.hanseter.json.editor.PropertiesEditResult
import com.github.hanseter.json.editor.types.TypeModel
import javafx.event.Event
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.layout.StackPane
import javafx.scene.text.Text

/**
 * An action that will be added as a button to the property sheet.
 */
interface EditorAction {

    val description: String

    /**
     * The priority of this option. The higher the priority, the further left it will be displayed in the UI.
     */
    val priority: Int
        get() = 0

    val selector: TargetSelector

    /**
     * Creates an icon for the button.
     * This needs to create a new [Node] every time it's called.
     */
    fun createIcon(size: Int): Node

    fun apply(
        input: PropertiesEditInput,
        model: TypeModel<*, *>,
        mouseEvent: Event?
    ): PropertiesEditResult?

    /**
     * Checks whether this action should be disabled, if the [selector] matches.
     * I.e. if this returns true the action will be visible but disabled.
     */
    fun shouldBeDisabled(model: TypeModel<*, *>): Boolean =
        model.schema.readOnly

    companion object {
        fun createTextIcon(text: String, size: Int): Node {
            val group = Group(Text(text)).apply {
                val origWidth = prefWidth(-1.0)
                val origHeight = prefHeight(-1.0)

                val scale = size / maxOf(origWidth, origHeight)

                scaleX = scale
                scaleY = scale
            }

            return StackPane(group).apply {
                minHeight = size.toDouble()
                maxHeight = size.toDouble()
                prefHeight = size.toDouble()
                minWidth = size.toDouble()
                maxWidth = size.toDouble()
                prefWidth = size.toDouble()
            }
        }
    }
}