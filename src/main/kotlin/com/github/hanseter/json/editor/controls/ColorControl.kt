package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.util.ColorStringConverter
import javafx.beans.binding.Bindings
import javafx.beans.property.Property
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ColorPicker

class ColorControl : ControlWithProperty<String?> {
    override val control = ColorPicker()
    override val property: Property<String?> = SimpleStringProperty(null)

    init {
        control.minHeight = 25.0
        Bindings.bindBidirectional(property, control.valueProperty(), ColorStringConverter)
    }

    override fun previewNull(b: Boolean) {
        control.promptText = if (b) TypeControl.NULL_PROMPT else ""
    }
}