package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.ui.TimeSpinner
import javafx.beans.property.Property

class LocalTimeControl : ControlWithProperty<String?> {
    override val control: TimeSpinner = TimeSpinner()
    override val property: Property<String?>
        get() = control.valueFactory.valueProperty()


    override fun previewNull(b: Boolean) {
        control.editor.promptText = if (b) TypeControl.NULL_PROMPT else ""
    }
}