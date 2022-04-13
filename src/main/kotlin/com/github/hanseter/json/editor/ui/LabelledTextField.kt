package com.github.hanseter.json.editor.ui

import com.sun.javafx.scene.control.behavior.TextFieldBehavior
import com.sun.javafx.scene.control.skin.TextFieldSkin
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.scene.control.Label
import javafx.scene.control.Skin
import javafx.scene.control.TextField
import javafx.scene.text.Text
import java.lang.Double.min


class LabelledTextField(text: String) : TextField(text) {

    private val labelProp: StringProperty = SimpleStringProperty("")

    constructor () : this("")

    fun labelProperty() = labelProp

    var label: String
        get() = labelProp.get()
        set(value) {
            labelProp.set(value)
        }

    override fun createDefaultSkin(): Skin<*> {
        return LabelledTextFieldSkin(this)
    }
}

class LabelledTextFieldSkin(
    private val textField: LabelledTextField,
    textFieldBehavior: TextFieldBehavior
) : TextFieldSkin(textField, textFieldBehavior) {

    constructor(textField: LabelledTextField) : this(textField, TextFieldBehavior(textField))

    companion object {
        const val LABEL_SEPARATOR_WIDTH = 5.0
    }

    private val label = Label().apply {
        textProperty().bind(textField.labelProperty())

        // if this is managed, the text field keeps growing when it receives focus
        // and it
        isManaged = false
        styleClass += "text-field-label"
    }

    private val textNode: Text?
    private val promptNode: Text?

    init {
        children.add(label)

        textNode = getParentField("textNode")
        promptNode = getParentField("promptNode")
    }

    private inline fun <reified T> getParentField(fieldName: String): T? {
        return try {
            val field = TextFieldSkin::class.java.getDeclaredField(fieldName)
            field.isAccessible = true
            field.get(this) as? T
        } catch (ex: Exception) {
            // this is just for the visuals, no need to react here
            null
        }
    }

    override fun layoutChildren(x: Double, y: Double, w: Double, h: Double) {

        val fullHeight = h + snappedTopInset() + snappedBottomInset()
        val textFieldStartX = snapPosition(x)

        if (textNode != null) {

            val activeTextNode = if (promptNode?.text?.isBlank() != false || !promptNode.isVisible) textNode else promptNode

            val prefTextWidth = snapSize(activeTextNode.prefWidth(fullHeight)) + LABEL_SEPARATOR_WIDTH

            val realTextWidth = if (label.text.isNullOrBlank()) w else min(prefTextWidth, w)

            val realLabelWidth = w - realTextWidth

            super.layoutChildren(textFieldStartX, 0.0, realTextWidth, fullHeight)

            label.resizeRelocate(textFieldStartX + realTextWidth, 0.0, realLabelWidth, fullHeight)

        } else {

            val prefLabelWidth = snapSize(label.prefWidth(fullHeight))

            val textFieldWidth = w - snapSize(prefLabelWidth)

            val realRightStart = w - prefLabelWidth + snappedLeftInset()

            super.layoutChildren(textFieldStartX, 0.0, textFieldWidth, fullHeight)

            label.resizeRelocate(realRightStart, 0.0, prefLabelWidth, fullHeight)
        }
    }
}