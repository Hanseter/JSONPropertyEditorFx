package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.ui.skins.ToggleSwitchSkin
import javafx.beans.binding.Bindings
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.WeakChangeListener
import javafx.scene.control.CheckBox
import org.controlsfx.control.ToggleSwitch

class BooleanControl : ControlWithProperty<Boolean?> {
    override val control: CheckBox = CheckBox().apply {
        skin = ToggleSwitchSkin(this)
        isIndeterminate = true
    }

    private val nullableProperty = SimpleObjectProperty<Boolean?>().apply {
        addListener{ _, _, newValue ->
            control.setValue(newValue)
        }
    }
    override val property: Property<Boolean?>
        get() = control.selectedProperty()

    override fun previewNull(b: Boolean) {
        control.text = if (b) TypeControl.NULL_PROMPT else ""
    }

    private fun CheckBox.setValue(newValue: Boolean?) {
        when (newValue) {
            true -> {
                selectedProperty().value=true
            }
            false -> {
                selectedProperty().value=false
            }
            null -> isIndeterminate = true
        }
    }
}

