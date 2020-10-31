package com.github.hanseter.json.editor.controls

import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.control.TextFormatter
import javafx.util.StringConverter
import org.everit.json.schema.NumberSchema
import java.text.DecimalFormat
import java.text.ParsePosition

class DoubleControl(schema: NumberSchema) : ControlWithProperty<Double?>, ChangeListener<Double?> {
    override val control = Spinner<Double?>()
    override val property: Property<Double?> = SimpleObjectProperty<Double?>(null)

    private val minInclusive = schema.minimum?.toDouble()
    private val minExclusive = schema.exclusiveMinimumLimit?.toDouble()
    private val maxInclusive = schema.maximum?.toDouble()
    private val maxExclusive = schema.exclusiveMaximumLimit?.toDouble()
    private val textFormatter = TextFormatter<String>(this::filterChange)

    init {
        control.minWidth = 150.0
        control.valueFactory = DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE)
        control.isEditable = true
        control.focusedProperty().addListener { _, _, new ->
            if (!new && (control.editor.text.isEmpty() || control.editor.text == "-")) {
                control.editor.text = control.valueFactory.converter.toString(property.value?.toDouble())
            }
        }
        control.editor.text = control.valueFactory.converter.toString(property.value?.toDouble())
        control.editor.textFormatterProperty().set(textFormatter)
        property.addListener(this)
    }

    private fun filterChange(change: TextFormatter.Change): TextFormatter.Change? {
        if (!change.isContentChange || change.controlNewText == "-") {
            return change
        }
        val parsePos = ParsePosition(0)
        val number = DECIMAL_FORMAT.parse(change.controlNewText, parsePos)?.toDouble()
        if (parsePos.index < change.controlNewText.length) {
            return null
        }

        return when {
            number == null -> {
                updateValueAndText(null, ""); null
            }
            maxInclusive != null && number > maxInclusive -> {
                updateValueAndText(maxInclusive); null
            }
            maxExclusive != null && number >= maxExclusive -> {
                updateValueAndText(maxExclusive - 1); null
            }
            minInclusive != null && number < minInclusive -> {
                updateValueAndText(minInclusive); null
            }
            minExclusive != null && number <= minExclusive -> {
                updateValueAndText(minExclusive + 1); null
            }
            else -> {
                property.value = number; change
            }
        }
    }

    private fun updateValueAndText(newValue: Double?, newText: String = DECIMAL_FORMAT.format(newValue)) {
        control.editor.textFormatterProperty().set(null)
        control.editor.text = newText
        property.removeListener(this)
        property.value = newValue
        property.addListener(this)
        control.editor.selectAll()
        control.editor.textFormatterProperty().set(textFormatter)
    }

    override fun previewNull(b: Boolean) {
        control.editor.promptText = if (b) TypeControl.NULL_PROMPT else ""

    }

    override fun changed(observable: ObservableValue<out Double?>?, oldValue: Double?, newValue: Double?) {
        control.editor.text = if (newValue == null) null
        else control.valueFactory.converter.toString(newValue.toDouble())
    }

    private class DoubleSpinnerValueFactory(min: Double, max: Double) : SpinnerValueFactory<Double?>() {
        val inner = DoubleSpinnerValueFactory(min, max)

        init {
            converter = CONVERTER
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

    companion object {
        private val DECIMAL_FORMAT = DecimalFormat("#0.################")

        private val CONVERTER = object : StringConverter<Double?>() {
            override fun toString(`object`: Double?): String? =
                    if (`object` == null) "" else DECIMAL_FORMAT.format(`object`)

            override fun fromString(string: String?): Double? =
                    if (string.isNullOrBlank()) null else DECIMAL_FORMAT.parse(string)?.toDouble()
        }
    }

}