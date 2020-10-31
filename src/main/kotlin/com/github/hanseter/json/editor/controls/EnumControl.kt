package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.types.EnumModel
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.control.ComboBox

//TODO this control makes every enum a string, even if it is something else. This needs to be improved.
class EnumControl(private val model: EnumModel) : ControlWithProperty<String?>, ChangeListener<String?> {
    override val control = ComboBox<String?>()
    override val property: Property<String?> = SimpleObjectProperty<String?>(null)

    init {
        control.minWidth = 150.0
        control.items.setAll(model.enumSchema.possibleValuesAsList.map { it.toString() })
        control.selectionModel.selectedIndexProperty()
                .addListener { _, _, new ->
                    if (new.toInt() >= 0) {
                        property.removeListener(this)
                        property.value = model.enumSchema.possibleValuesAsList[new.toInt()].toString()
                        property.addListener(this)
                    }
                }
        property.addListener(this)
    }

    override fun previewNull(b: Boolean) {
        control.promptText = if (b) TypeControl.NULL_PROMPT else ""
    }

    override fun changed(observable: ObservableValue<out String?>?, oldValue: String?, newValue: String?) {
        setSelectedValue(newValue)
    }

    private fun setSelectedValue(value: String?) {
        if (control.items.contains(value)) {
            control.selectionModel.select(value)
        } else {
            control.selectionModel.select(model.defaultValue)
        }
    }

}