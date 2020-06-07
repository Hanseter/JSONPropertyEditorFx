package com.github.hanseter.json.editor.controls

import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory
import org.everit.json.schema.NumberSchema
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.control.TextFormatter
import java.text.ParsePosition
import java.text.DecimalFormat
import javafx.beans.property.ReadOnlyIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import com.github.hanseter.json.editor.extensions.SchemaWrapper

class IntegerControl(schema: SchemaWrapper<NumberSchema>) :
	RowBasedControl<NumberSchema, Int, Spinner<Int>>(
		schema,
		Spinner(
			schema.schema.getMinimum()?.toInt() ?: (schema.schema.getExclusiveMinimumLimit()?.toInt()?.inc())
			?: Int.MIN_VALUE,
			schema.schema.getMaximum()?.toInt() ?: (schema.schema.getExclusiveMaximumLimit()?.toInt()?.dec())
			?: Int.MAX_VALUE,
			0
		),
		{ it.valueFactory.valueProperty() },
		schema.schema.getDefaultValue() as? Int
	) {


	init {
		control.setEditable(true)
		control.editor.textProperty().addListener { _, _, new ->
			if (new.isNotEmpty() && new != "-") {
				try {
					control.increment(0); // won't change value, but will commit editor
				} catch (e: NumberFormatException) {
					control.getEditor().text = control.valueFactory.value.toString()
				}
			}
		}
		control.focusedProperty().addListener { _, _, new ->
			if (!new && (control.editor.text.isEmpty() || control.editor.text == "-")) {
				control.editor.text = control.valueFactory.value.toString()
			}

		}
	}
}