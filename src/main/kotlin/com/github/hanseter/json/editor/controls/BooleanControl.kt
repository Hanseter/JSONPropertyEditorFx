package com.github.hanseter.json.editor.controls

import javafx.beans.property.Property
import org.controlsfx.control.ToggleSwitch

class BooleanControl : ControlWithProperty<Boolean?> {
    override val control: ToggleSwitch = ToggleSwitch()
    override val property: Property<Boolean?>
        get() = control.selectedProperty()

    override fun previewNull(b: Boolean) {
        control.text = if (b) TypeControl.NULL_PROMPT else ""
    }
}

