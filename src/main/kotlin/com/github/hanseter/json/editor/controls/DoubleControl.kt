package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.actions.ActionsContainer
import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.control.TextFormatter
import javafx.util.StringConverter
import org.everit.json.schema.NumberSchema
import java.text.DecimalFormat
import java.text.ParsePosition

class DoubleControl(override val schema: SchemaWrapper<NumberSchema>, context: EditorContext) : TypeControl, ControlProvider<Double> {
    override val control = Spinner<Double>()
    override val value = SimpleObjectProperty<Double?>(null)
    override val defaultValue: Double?
        get() = schema.schema.defaultValue as? Double
    override val editorActionsContainer: ActionsContainer = context.createActionContainer(this)

    private val delegate = RowBasedControl(this)

    override val node: FilterableTreeItem<TreeItemData> = delegate.node
    override val valid: ObservableBooleanValue = SimpleBooleanProperty(true)

    private val minInclusive = schema.schema.minimum?.toDouble()
    private val minExclusive = schema.schema.exclusiveMinimumLimit?.toDouble()
    private val maxInclusive = schema.schema.maximum?.toDouble()
    private val maxExclusive = schema.schema.exclusiveMaximumLimit?.toDouble()
    private val textFormatter = TextFormatter<String>(this::filterChange)

    init {
        control.minWidth = 150.0
        control.valueFactory = NewNullSafeDoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE)
        control.isEditable = true
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
                if (delegate.isRequired) updateValueAndText(0.0, "0") else updateValueAndText(null, ""); null
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


    override fun bindTo(type: BindableJsonType) {
        if (delegate.bindTo(type, DOUBLE_CONVERTER)) {
            valueNewlyBound()
        }
        control.editor.promptText = if (delegate.isBoundToNull()) TypeControl.NULL_PROMPT else ""
    }

    private fun valueNewlyBound() {
        val value = value.value
        control.editor.text = if (value == null) ""
        else control.valueFactory.converter.toString(value.toDouble())
    }

    private class NewNullSafeDoubleSpinnerValueFactory(min: Double, max: Double) : SpinnerValueFactory<Double>() {

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
        val DOUBLE_CONVERTER: (Any?) -> Double? = { (it as? Number)?.toDouble() }

        private val CONVERTER = object : StringConverter<Double?>() {
            override fun toString(`object`: Double?): String? =
                    if (`object` == null) "" else DECIMAL_FORMAT.format(`object`)

            override fun fromString(string: String?): Double? =
                    if (string.isNullOrBlank()) null else DECIMAL_FORMAT.parse(string)?.toDouble()
        }
    }

}