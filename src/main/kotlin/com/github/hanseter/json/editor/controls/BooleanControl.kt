package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.ui.skins.ToggleSwitchSkin
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.control.CheckBox

class BooleanControl : ControlWithProperty<Boolean?>, ChangeListener<Boolean?> {
    override val control: CheckBox = CheckBox().apply {
        skin = ToggleSwitchSkin(this)
        isIndeterminate=true
    }

    override val property = SimpleObjectProperty<Boolean?>(null)

    init {
        property.addListener(this)
        control.selectedProperty().addListener { _, _, new ->
            property.removeListener(this)
            property.value = new
            property.addListener(this)
        }
    }

    override fun previewNull(b: Boolean) {
        control.text = if (b) TypeControl.NULL_PROMPT else ""
    }

    override fun changed(
        observable: ObservableValue<out Boolean?>?,
        oldValue: Boolean?,
        newValue: Boolean?
    ) {
        setNullableBoolean(newValue)
    }

    private fun setNullableBoolean(newValue: Boolean?) {
        when (newValue) {
            true -> {
                control.isSelected = true
                control.isIndeterminate = false
            }

            false -> {
                control.isSelected = false
                control.isIndeterminate = false
            }

            null -> {
                control.isIndeterminate = true
                control.isSelected = false
            }
        }
    }
}

