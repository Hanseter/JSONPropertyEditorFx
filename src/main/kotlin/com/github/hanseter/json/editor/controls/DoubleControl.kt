package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory
import javafx.scene.control.TextFormatter
import javafx.util.StringConverter
import org.everit.json.schema.NumberSchema
import java.text.DecimalFormat
import java.text.ParsePosition

class DoubleControl(schema: SchemaWrapper<NumberSchema>) :
	RowBasedControl<NumberSchema, Number, Spinner<Double>>(
		schema,
		Spinner(),
		SimpleObjectProperty<Number>(null),
		schema.schema.defaultValue as? Double
	) {
	private val minInclusive = schema.schema.minimum?.toDouble()
	private val minExclusive = schema.schema.exclusiveMinimumLimit?.toDouble()
	private val maxInclusive = schema.schema.maximum?.toDouble()
	private val maxExclusive = schema.schema.exclusiveMaximumLimit?.toDouble()
	private val textFormatter = TextFormatter<String>(this::filterChange)

	init {
		control.minWidth = 150.0
		control.valueFactory = DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE)
		control.isEditable = true
		control.valueFactory.converter = CONVERTER
		control.focusedProperty().addListener { _, _, new ->
			if (!new && (control.editor.text.isEmpty() || control.editor.text == "-")) {
				control.editor.text = control.valueFactory.converter.toString(value.value?.toDouble())
			}

		}
		control.editor.text = control.valueFactory.converter.toString(value.value?.toDouble())
		control.editor.textFormatterProperty().set(textFormatter)
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
				updateValueAndText(0.0, "0"); null
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
				value.value = number; change
			}
		}
	}

	private fun updateValueAndText(newValue: Double, newText: String = DECIMAL_FORMAT.format(newValue)) {
		control.editor.textFormatterProperty().set(null)
		control.editor.text = newText
		value.value = newValue
		control.editor.selectAll()
		control.editor.textFormatterProperty().set(textFormatter)
	}


	protected override fun valueNewlyBound() {
		val foo = value.getValue()
		val value = foo?.toDouble()
		control.editor.text = if (value == null) {
			""
		} else {
			control.valueFactory.converter.toString(value.toDouble())
		}
	}

	private class NumberSpinnerValueFactory() : SpinnerValueFactory<Number>() {
		override fun increment(steps: Int) {
//		    val currentValue = BigDecimal.valueOf(getValue());
//            final BigDecimal minBigDecimal = BigDecimal.valueOf(getMin());
//            final BigDecimal maxBigDecimal = BigDecimal.valueOf(getMax());
//            final BigDecimal amountToStepByBigDecimal = BigDecimal.valueOf(getAmountToStepBy());
//            BigDecimal newValue = currentValue.add(amountToStepByBigDecimal.multiply(BigDecimal.valueOf(steps)));
//            setValue(newValue.compareTo(maxBigDecimal) <= 0 ? newValue.doubleValue() :
//                    (isWrapAround() ? Spinner.wrapValue(newValue, minBigDecimal, maxBigDecimal).doubleValue() : getMax()));
		}

		override fun decrement(steps: Int) {
//			        @Override public void decrement(int steps) {
//            final BigDecimal currentValue = BigDecimal.valueOf(getValue());
//            final BigDecimal minBigDecimal = BigDecimal.valueOf(getMin());
//            final BigDecimal maxBigDecimal = BigDecimal.valueOf(getMax());
//            final BigDecimal amountToStepByBigDecimal = BigDecimal.valueOf(getAmountToStepBy());
//            BigDecimal newValue = currentValue.subtract(amountToStepByBigDecimal.multiply(BigDecimal.valueOf(steps)));
//            setValue(newValue.compareTo(minBigDecimal) >= 0 ? newValue.doubleValue() :
//                    (isWrapAround() ? Spinner.wrapValue(newValue, minBigDecimal, maxBigDecimal).doubleValue() : getMin()));
//        }
//
//        /** {@inheritDoc} */
//        @Override public void increment(int steps) {
		}
	}

	companion object {
		private val DECIMAL_FORMAT = DecimalFormat("#0.################")
		private val CONVERTER = object : StringConverter<Double?>() {
			override fun toString(`object`: Double?): String? =
				if (`object` == null) "" else DECIMAL_FORMAT.format(`object`)

			override fun fromString(string: String?): Double? =
				if (string == null) null else DECIMAL_FORMAT.parse(string)?.toDouble()
		}
	}

}