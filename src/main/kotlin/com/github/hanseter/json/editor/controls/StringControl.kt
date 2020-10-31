package com.github.hanseter.json.editor.controls

import javafx.beans.property.Property
import javafx.scene.control.TextField

class StringControl : ControlWithProperty<String?> {
    override val control: TextField = TextField()
    override val property: Property<String?>
        get() = control.textProperty()

    override fun previewNull(b: Boolean) {
        control.promptText = if (b) TypeControl.NULL_PROMPT else ""
    }
}