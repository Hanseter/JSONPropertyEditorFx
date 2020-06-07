package com.github.hanseter.json.editor.controls

import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory
import org.everit.json.schema.NumberSchema
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory
import javafx.scene.control.TextFormatter
import java.util.function.UnaryOperator
import java.text.ParseException
import java.text.DecimalFormat
import java.text.ParsePosition
import javafx.beans.property.SimpleDoubleProperty
import javafx.application.Platform
import javafx.util.StringConverter
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.Property
import com.github.hanseter.json.editor.extensions.SchemaWrapper

class DoubleControl(schema: SchemaWrapper<NumberSchema>) :
	RowBasedControl<NumberSchema, Number, Spinner<Double>>(
		schema,
		Spinner<Double>(),
		SimpleObjectProperty<Number>(null),
		schema.schema.getDefaultValue() as? Double
	) {
	private val minInclusive = schema.schema.getMinimum()?.toDouble()
	private val minExclusive = schema.schema.getExclusiveMinimumLimit()?.toDouble()
	private val maxInclusive = schema.schema.getMaximum()?.toDouble()
	private val maxExclusive = schema.schema.getExclusiveMaximumLimit()?.toDouble()
	private val textFormatter = TextFormatter<String>(this::filterChange)

	init {
		control.setValueFactory(DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE))
		control.setEditable(true)
		control.valueFactory.setConverter(CONVERTER)
		control.focusedProperty().addListener { _, _, new ->
			if (!new && (control.editor.text.isEmpty() || control.editor.text == "-")) {
				control.editor.text = control.valueFactory.converter.toString(value.getValue()?.toDouble())
			}

		}
		control.editor.text = control.valueFactory.converter.toString(value.getValue()?.toDouble())
		control.editor.textFormatterProperty().set(textFormatter)
	}

	private fun filterChange(change: TextFormatter.Change): TextFormatter.Change? {
		if (!change.isContentChange() || change.getControlNewText() == "-") {
			return change
		}
		val parsePos = ParsePosition(0)
		val number = DECIMAL_FORMAT.parse(change.getControlNewText(), parsePos)?.toDouble()
		if (parsePos.getIndex() < change.getControlNewText().length) {
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
				value.setValue(number); change
			}
		}
	}

	private fun updateValueAndText(newValue: Double, newText: String = DECIMAL_FORMAT.format(newValue)) {
		control.editor.textFormatterProperty().set(null)
		control.editor.text = newText
		value.setValue(newValue)
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
				if (`object` == null) null else DECIMAL_FORMAT.format(`object`)

			override fun fromString(string: String?): Double? =
				if (string == null) null else DECIMAL_FORMAT.parse(string)?.toDouble()
		}
	}

}