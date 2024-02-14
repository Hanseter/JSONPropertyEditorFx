package com.github.hanseter.json.editor.controls

import javafx.beans.property.Property
import javafx.scene.control.TextArea

class MultiLineStringControl : ControlWithProperty<String?> {
    override val control: TextArea = TextArea().apply {
        maxHeight = 26.0
        prefHeightProperty().bind(maxHeightProperty())
        minHeightProperty().bind(maxHeightProperty())
        textProperty().addListener { _, _, text ->
            maxHeight = if ('\n' in text) 70.0
            else 26.0
        }
    }
    override val property: Property<String?>
        get() = control.textProperty()


    override fun previewNull(b: Boolean) {
        control.promptText = if (b) TypeControl.NULL_PROMPT else ""
    }
}