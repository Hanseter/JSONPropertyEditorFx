package com.github.hanseter.json.editor.controls

import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.util.StringConverter
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParsePosition

class DoubleControl(decimalFormatSymbols: DecimalFormatSymbols) : NumberControl<Double?>() {

    private val decimalFormat = DecimalFormat(DEFAULT_PATTERN).apply {
        this.decimalFormatSymbols = decimalFormatSymbols
    }

    override val control = Spinner<Double?>().apply {
        valueFactory = DoubleSpinnerValueFactory(StringDoubleConverter(decimalFormat))
        isEditable = true
    }

    init {
        initControl()
    }

    private class DoubleSpinnerValueFactory(converter: StringConverter<Double?>) :
        SpinnerValueFactory<Double?>() {

        val inner = DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE).apply {
            this.converter = converter
        }

        init {
            this.converter = converter
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

    private class StringDoubleConverter(private val decimalFormat: DecimalFormat) :
        StringConverter<Double?>() {
        override fun toString(`object`: Double?): String? {
            return when (`object`) {
                null -> ""
                -0.0 -> "0.0"
                else -> decimalFormat.format(`object`)
            }
        }


        override fun fromString(string: String?): Double? {
            if (string.isNullOrBlank()) return 0.0
            val parsePosition = ParsePosition(0)
            val number = decimalFormat.parse(string, parsePosition)
            if (parsePosition.index != string.length) {
                throw java.lang.NumberFormatException()
            }
            return number?.toDouble()
        }

    }

    companion object {
        const val DEFAULT_PATTERN = "#0.################"
    }

}