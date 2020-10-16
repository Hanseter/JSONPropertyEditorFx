package com.github.hanseter.json.editor.util

import com.sun.javafx.scene.control.skin.TextFieldSkin
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.scene.control.Label
import javafx.scene.control.Skin
import javafx.scene.control.TextField
import javafx.scene.text.Text
import kotlin.math.min


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

class LabelledTextFieldSkin(private val textField: LabelledTextField) : TextFieldSkin(textField) {

    private val label = Label().apply {
        textProperty().bind(textField.labelProperty())

        // if this is managed, the text field keeps growing when it receives focus
        // and it
        isManaged = false
        styleClass += "text-field-label"
    }

    private val textNode: Text?

    init {
        children.add(label)

        textNode = try {
            val field = TextFieldSkin::class.java.getDeclaredField("textNode")
            field.isAccessible = true
            field.get(this) as? Text
        } catch (ex: Exception) {
            // this is just for the visuals, no need to react here
            null
        }
    }


    override fun layoutChildren(x: Double, y: Double, w: Double, h: Double) {

        val fullHeight = h + snappedTopInset() + snappedBottomInset()

        val rightWidth = snapSize(label.prefWidth(fullHeight))

        val textFieldStartX = snapPosition(x)
        val textFieldWidth = w - snapSize(rightWidth)

        super.layoutChildren(textFieldStartX, 0.0, textFieldWidth, fullHeight)

        val fallbackRightStart = w - rightWidth + snappedLeftInset()

        val realRightStart = if (textNode != null) min(textNode.prefWidth(h) + 10.0, fallbackRightStart) else fallbackRightStart

        label.resizeRelocate(realRightStart, 0.0, rightWidth, fullHeight)
    }
}