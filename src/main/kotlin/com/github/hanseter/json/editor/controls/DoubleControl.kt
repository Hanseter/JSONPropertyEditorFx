package com.github.hanseter.json.editor.controls

import javafx.application.Platform
import javafx.beans.property.Property
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.util.StringConverter
import org.everit.json.schema.NumberSchema
import java.text.DecimalFormat
import java.text.ParsePosition

class DoubleControl(schema: NumberSchema) : ControlWithProperty<Double?> {

    private val converter = StringDoubleConverter()

    override val control = Spinner<Double?>().apply {
        minWidth = 150.0
        valueFactory = DoubleSpinnerValueFactory()
        isEditable = true
    }
    override val property: Property<Double?> = control.valueFactory.valueProperty()

    init {
        control.focusedProperty().addListener { _, _, new ->
            if (!new && (control.editor.text.isEmpty() || control.editor.text == "-")) {
                control.editor.text =
                    control.valueFactory.converter.toString(property.value?.toDouble())
            }
        }
        control.editor.textProperty().addListener { _,_,_ ->
            updateValueAfterTextChange(control)
        }
    }


    override fun previewNull(b: Boolean) {
        control.editor.promptText = if (b) TypeControl.NULL_PROMPT else ""
    }

    private inner class DoubleSpinnerValueFactory : SpinnerValueFactory<Double?>() {
        val inner = DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE)

        init {
            this.converter = this@DoubleControl.converter
            inner.converter = this.converter
        }

        override fun decrement(steps: Int) {
            if (value != null) {
                inner.value = value
                inner.decrement(steps)
                value = inner.value
            }
        }

        override fun increment(steps: Int) {
            if (value != null) {
                inner.value = value
                inner.increment(steps)
                value = inner.value
            }
        }

    }

    private inner class StringDoubleConverter : StringConverter<Double?>() {
        override fun toString(`object`: Double?): String? =
            if (`object` == null) "" else DECIMAL_FORMAT.format(`object`)

        override fun fromString(string: String?): Double? {
            if (string.isNullOrBlank()) return 0.0
            val parsePosition = ParsePosition(0)
            val number = DECIMAL_FORMAT.parse(string, parsePosition)
            if (parsePosition.index != string.length) {
                throw java.lang.NumberFormatException()
            }
            return number?.toDouble()
        }

    }

    companion object {
        private val DECIMAL_FORMAT = DecimalFormat("#0.################")

        fun <T> updateValueAfterTextChange(control: Spinner<T>) {
            Platform.runLater {
                val new = control.editor.text
                if (new.isNotEmpty() && new != "-") {
                    try {
                        val caretPos = control.editor.caretPosition
                        control.increment(0) // won't change value, but will commit editor
                        control.editor.text = new
                        control.editor.positionCaret(caretPos)
                    } catch (e: NumberFormatException) {
                        control.editor.text = control.valueFactory.converter.toString(control.valueFactory.value)
                            ?: "0"
                    }
                }
            }
        }
    }

}