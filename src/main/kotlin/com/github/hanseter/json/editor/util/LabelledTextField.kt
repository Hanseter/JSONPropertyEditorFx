package com.github.hanseter.json.editor.util

import com.sun.javafx.scene.control.skin.TextFieldSkin
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.scene.control.Label
import javafx.scene.control.Skin
import javafx.scene.control.TextField


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
        isManaged = false
        styleClass += "text-field-label"
    }

    init {
        children.add(label)
    }


    override fun layoutChildren(x: Double, y: Double, w: Double, h: Double) {

        val fullHeight = h + snappedTopInset() + snappedBottomInset()

        val leftWidth = 0.0
        val rightWidth = snapSize(label.prefWidth(fullHeight))

        val textFieldStartX = snapPosition(x) + snapSize(leftWidth)
        val textFieldWidth = w - snapSize(leftWidth) - snapSize(rightWidth)

        super.layoutChildren(textFieldStartX, 0.0, textFieldWidth, fullHeight)

        val rightStartX = w - rightWidth + snappedLeftInset()
        label.resizeRelocate(rightStartX, 0.0, rightWidth, fullHeight)
    }
}