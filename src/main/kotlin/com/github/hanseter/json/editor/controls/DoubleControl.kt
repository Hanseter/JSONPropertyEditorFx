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
	RowBasedControl<NumberSchema, Double, Spinner<Double>>(
		schema,
		Spinner(),
		SimpleObjectProperty<Double>(0.0),
		schema.schema.getDefaultValue() as? Double ?: 0.0
	) {
	private val minInclusive = schema.schema.getMinimum()?.toDouble()
	private val minExclusive = schema.schema.getExclusiveMinimumLimit()?.toDouble()
	private val maxInclusive = schema.schema.getMaximum()?.toDouble()
	private val maxExclusive = schema.schema.getExclusiveMaximumLimit()?.toDouble()

	init {
		control.setValueFactory(DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE))
		control.setEditable(true)
		control.valueFactory.setConverter(CONVERTER)
		control.focusedProperty().addListener { _, _, new ->
			if (!new && (control.editor.text.isEmpty() || control.editor.text == "-")) {
				control.editor.text = control.valueFactory.converter.toString(value.getValue())
			}

		}
		control.editor.textFormatterProperty()
			.set(TextFormatter<String>(this::filterChange))
		control.editor.text = control.valueFactory.converter.toString(value.getValue())
	}

	private fun filterChange(change: TextFormatter.Change): TextFormatter.Change? {
		if (!change.isContentChange() || change.getControlNewText() == "-") {
			return change
		}
		val parsePos = ParsePosition(0)
		val number = DECIMAL_FORMAT.parse(change.getControlNewText(), parsePos)?.toDouble()
		if (number == null || parsePos.getIndex() < change.getControlNewText().length) {
			return null
		}
		if (maxInclusive != null && number > maxInclusive) {
			control.editor.text = DECIMAL_FORMAT.format(maxInclusive)
			return null
		}
		if (maxExclusive != null && number >= maxExclusive) {
			control.editor.text = DECIMAL_FORMAT.format(maxExclusive - 1)
			return null
		}
		if (minInclusive != null && number < minInclusive) {
			control.editor.text = DECIMAL_FORMAT.format(minInclusive)
			return null
		}
		if (minExclusive != null && number <= minExclusive) {
			control.editor.text = DECIMAL_FORMAT.format(minExclusive + 1)
			return null
		}
		value.setValue(number)
		return change
	}


	protected override fun valueNewlyBound() {
		control.editor.text = control.valueFactory.converter.toString(value.getValue().toDouble())
	}

	companion object {
		private val DECIMAL_FORMAT = DecimalFormat("#0.################")
		private val CONVERTER = object : StringConverter<Double>() {
			override fun toString(`object`: Double?): String? = DECIMAL_FORMAT.format(`object`)

			override fun fromString(string: String?): Double? = DECIMAL_FORMAT.parse(string)?.toDouble()
		}
	}

}