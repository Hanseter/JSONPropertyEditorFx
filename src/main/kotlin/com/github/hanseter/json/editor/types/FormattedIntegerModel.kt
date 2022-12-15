/*
 * SWEETS - Software Engineering Tooling Suite
 *
 * Copyright (c) Siemens Mobility GmbH 2022, All Rights Reserved, Confidential.
 */
package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.controls.DoubleControl
import com.github.hanseter.json.editor.extensions.EffectiveSchema
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
class FormattedIntegerModel(
    schema: EffectiveSchema<NumberSchema>,
    decimalFormatSymbols: DecimalFormatSymbols
) : IntegerModel(schema) {

    private val intFormat = IntFormat(schema.baseSchema.unprocessedProperties, decimalFormatSymbols)

    override val previewString: PreviewString
        get() = when {
            value != null -> PreviewString(intFormat.converter.toString(value))
            defaultValue != null -> PreviewString(intFormat.converter.toString(defaultValue))
            else -> PreviewString.NO_VALUE
        }

    companion object {
        const val INT_FORMAT = "int-format"
        private const val INT_FORMAT_PATTERN = "pattern"
        private const val INT_FORMAT_PRECISION = "precision"

        class IntFormat(
            unprocessedProperties: Map<String, Any>,
            decimalFormatSymbols: DecimalFormatSymbols
        ) {
            private val map = unprocessedProperties[INT_FORMAT] as Map<*, *>
            val pattern = (map[INT_FORMAT_PATTERN] as? String)
                ?: DoubleControl.DEFAULT_PATTERN
            val symbols = decimalFormatSymbols
            val precision = (map[INT_FORMAT_PRECISION] as? Int) ?: 0

            val multiplier = 10.0.pow(precision)

            val decimalFormat = DecimalFormat(pattern).apply {
                this.decimalFormatSymbols = symbols
            }

            val converter = object : StringConverter<Int?>() {
                override fun toString(value: Int?): String {
                    if (value == null) return ""
                    return decimalFormat.format((value.toDouble() / multiplier))
                }

                override fun fromString(string: String?): Int? {
                    if (string.isNullOrBlank()) return 0
                    val parsePosition = ParsePosition(0)
                    val parsed = decimalFormat.parse(string, parsePosition)
                    val number = (parsed?.toDouble()?.times(multiplier))?.roundToInt()

                    if (parsePosition.index != string.length) {
                        throw java.lang.NumberFormatException()
                    }
                    return number
                }
            }
        }
    }
}