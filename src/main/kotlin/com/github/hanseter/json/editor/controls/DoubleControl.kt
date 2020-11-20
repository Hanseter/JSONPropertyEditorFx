package com.github.hanseter.json.editor.controls

import javafx.beans.property.Property
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.util.StringConverter
import org.everit.json.schema.NumberSchema
import java.text.DecimalFormat
import java.text.ParsePosition

class DoubleControl(schema: NumberSchema) : ControlWithProperty<Double?> {
    private val minInclusive = schema.minimum?.toDouble() ?: -Double.MAX_VALUE
    private val minExclusive = schema.exclusiveMinimumLimit?.toDouble() ?: -Double.MAX_VALUE
    private val maxInclusive = schema.maximum?.toDouble() ?: Double.MAX_VALUE
    private val maxExclusive = schema.exclusiveMaximumLimit?.toDouble() ?: Double.MAX_VALUE

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
                control.editor.text = control.valueFactory.converter.toString(property.value?.toDouble())
            }
        }
        control.editor.textProperty().addListener { _, _, new ->
            if (new.isEmpty()) {
                control.increment(0)
            } else if (new.isNotEmpty() && new != "-") {
                try {
                    converter.failOnTrailingSeparator = true
                    control.increment(0) // won't change value, but will commit editor
                } catch (e: NumberFormatException) {
                    control.editor.text = control.valueFactory.converter.toString(property.value)
                            ?: "0"
                } catch (e: TrailingSeparatorException) {
                    //Nothing to do
                } finally {
                    converter.failOnTrailingSeparator = false
                }
            }
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
                coerceValue()
                value = inner.value
            }
        }

        override fun increment(steps: Int) {
            if (value != null) {
                inner.value = value
                inner.increment(steps)
                coerceValue()
                value = inner.value
            }
        }

        private fun coerceValue() {
            if (inner.value > maxInclusive) {
                inner.value = maxInclusive
            }
            if (inner.value >= maxExclusive) {
                inner.value = maxExclusive - 1
            }
            if (inner.value < minInclusive) {
                inner.value = minInclusive
            }
            if (inner.value < minExclusive) {
                inner.value = minExclusive + 1
            }
        }

    }

    private inner class StringDoubleConverter : StringConverter<Double?>() {
        var failOnTrailingSeparator = false
        override fun toString(`object`: Double?): String? =
                if (`object` == null) "" else DECIMAL_FORMAT.format(`object`)

        override fun fromString(string: String?): Double? {
            if (string.isNullOrBlank()) return null
            val parsePosition = ParsePosition(0)
            val number = DECIMAL_FORMAT.parse(string, parsePosition)
            if (parsePosition.index != string.length) {
                throw java.lang.NumberFormatException()
            }
            if (failOnTrailingSeparator && isTrailingSeparator(string)) {
                throw TrailingSeparatorException()
            }
            return number?.toDouble()
        }

        private fun isTrailingSeparator(string: String): Boolean =
                string.last() == DECIMAL_FORMAT.decimalFormatSymbols.decimalSeparator
    }

    class TrailingSeparatorException : Exception()

    companion object {
        private val DECIMAL_FORMAT = DecimalFormat("#0.################")
    }

}