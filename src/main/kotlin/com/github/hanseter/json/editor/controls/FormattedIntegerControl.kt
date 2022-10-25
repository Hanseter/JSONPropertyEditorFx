/*
 * SWEETS - Software Engineering Tooling Suite
 *
 * Copyright (c) Siemens Mobility GmbH 2022, All Rights Reserved, Confidential.
 */
package com.github.hanseter.json.editor.controls

import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.util.StringConverter
import org.everit.json.schema.NumberSchema
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParsePosition
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 *
 * @author Henrik Frühling (henrik.fruehling@siemens.com)
 */
class FormattedIntegerControl(schema: NumberSchema, decimalFormatSymbols: DecimalFormatSymbols) :
    NumberControl<Int?>() {

    companion object {
        const val INT_FORMAT = "int-format"
        const val INT_FORMAT_PATTERN = "pattern"
        const val INT_FORMAT_PRECISION = "precision"
    }

    private val intFormat = IntFormat(schema.unprocessedProperties, decimalFormatSymbols)

    private class IntFormat(
        unprocessedProperties: Map<String, Any>,
        decimalFormatSymbols: DecimalFormatSymbols
    ) {
        private val map = unprocessedProperties[INT_FORMAT] as Map<*, *>
        val pattern = (map[INT_FORMAT_PATTERN] as? String) ?: DoubleControl.DEFAULT_PATTERN
        val symbols = decimalFormatSymbols
        val precision = (map[INT_FORMAT_PRECISION] as? Int) ?: 0
    }

    override val control: Spinner<Int?> =
        Spinner<Int?>(FormattedIntegerSpinnerValueFactoryNullSafe(intFormat)).apply {
            isEditable = true
        }


    init {
        initControl()
    }

    private class FormattedIntegerSpinnerValueFactoryNullSafe(intFormat: IntFormat) :
        SpinnerValueFactory<Int?>() {
        private val multiplier = 10.0.pow(intFormat.precision)

        private val format = DecimalFormat(intFormat.pattern).apply {
            decimalFormatSymbols = intFormat.symbols
        }

        init {
            converter = object : StringConverter<Int?>() {
                override fun toString(value: Int?): String {
                    if (value == null) return ""
                    return format.format((value.toDouble() / multiplier))
                }

                override fun fromString(string: String?): Int? {
                    if (string.isNullOrBlank()) return 0
                    val parsePosition = ParsePosition(0)
                    val parsed = format.parse(string, parsePosition)
                    val number = (parsed?.toDouble()?.times(multiplier))?.roundToInt()

                    if (parsePosition.index != string.length) {
                        throw java.lang.NumberFormatException()
                    }
                    return number
                }
            }
        }

        override fun increment(steps: Int) {
            value = value?.plus((steps * multiplier).toInt())
        }

        override fun decrement(steps: Int) {
            value = value?.minus((steps * multiplier).toInt())
        }
    }

    override fun previewNull(b: Boolean) {
        control.editor.promptText = if (b) TypeControl.NULL_PROMPT else ""
    }
}


