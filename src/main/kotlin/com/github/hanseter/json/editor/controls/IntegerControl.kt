package com.github.hanseter.json.editor.controls

import javafx.beans.property.Property
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.util.converter.IntegerStringConverter
import org.everit.json.schema.NumberSchema

class IntegerControl(schema: NumberSchema) : ControlWithProperty<Int?> {
    override val control = Spinner<Int>(IntegerSpinnerValueFactoryNullSafe())
    override val property: Property<Int?>
        get() = control.valueFactory.valueProperty()

    init {
        control.isEditable = true
        control.editor.textProperty().addListener { _, _, new ->
            if (new.isEmpty()) {
                control.increment(0)
            } else if (new.isNotEmpty() && new != "-") {
                try {
                    control.increment(0) // won't change value, but will commit editor
                } catch (e: NumberFormatException) {
                    control.editor.text = control.valueFactory.value?.toString() ?: "0"
                }
            }
        }
        control.focusedProperty().addListener { _, _, new ->
            if (!new && (control.editor.text.isEmpty() || control.editor.text == "-")) {
                control.editor.text = control.valueFactory.value?.toString() ?: ""
            }
        }
    }

    class IntegerSpinnerValueFactoryNullSafe() : SpinnerValueFactory<Int?>() {
        init {
            converter = IntegerStringConverter()
        }

        override fun increment(steps: Int) {
            value = value?.plus(steps)
        }

        override fun decrement(steps: Int) {
            value = value?.minus(steps)
        }
    }

    override fun previewNull(b: Boolean) {
        control.editor.promptText = if (b) TypeControl.NULL_PROMPT else ""
    }
}