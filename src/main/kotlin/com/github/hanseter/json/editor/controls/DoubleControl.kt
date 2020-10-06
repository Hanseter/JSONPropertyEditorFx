package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.actions.EditorAction
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonType
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.control.TextFormatter
import javafx.util.StringConverter
import org.everit.json.schema.NumberSchema
import java.text.DecimalFormat
import java.text.ParsePosition

class DoubleControl(schema: SchemaWrapper<NumberSchema>, actions: List<EditorAction>) :
        RowBasedControl<NumberSchema, Number, Spinner<Double>>(
                schema,
                Spinner(),
                SimpleObjectProperty<Number>(null),
                schema.schema.defaultValue as? Double,
                actions
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
                if (isRequired) updateValueAndText(0.0, "0") else updateValueAndText(null, ""); null
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

    private fun updateValueAndText(newValue: Double?, newText: String = DECIMAL_FORMAT.format(newValue)) {
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

    override fun bindTo(type: BindableJsonType) {
        super.bindTo(type)
        control.editor.promptText = if (isBoundToNull()) TypeControl.NULL_PROMPT else ""
    }

    private class DoubleSpinnerValueFactory(min: Double, max: Double) : SpinnerValueFactory.DoubleSpinnerValueFactory(min, max) {

        override fun increment(steps: Int) {
            if (value != null) super.increment(steps)
        }

        override fun decrement(steps: Int) {
            if (value != null) super.decrement(steps)
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