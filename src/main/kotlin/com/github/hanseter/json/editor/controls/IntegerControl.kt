package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import javafx.beans.value.ChangeListener
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import javafx.util.converter.IntegerStringConverter
import org.everit.json.schema.NumberSchema

class IntegerControl(schema: SchemaWrapper<NumberSchema>) :
        RowBasedControl<NumberSchema, Int, Spinner<Int?>>(
                schema,
                Spinner(
                        IntegerSpinnerValueFactoryNullSafe(
                                schema.schema.minimum?.toInt()
                                        ?: (schema.schema.exclusiveMinimumLimit?.toInt()?.inc())
                                        ?: Int.MIN_VALUE,
                                schema.schema.maximum?.toInt()
                                        ?: (schema.schema.exclusiveMaximumLimit?.toInt()?.dec())
                                        ?: Int.MAX_VALUE)
                ),
                { it.valueFactory.valueProperty() },
                schema.schema.defaultValue as? Int
        ) {

    init {
        control.isEditable = true
        control.editor.textProperty().addListener { _, _, new ->
            if (new.isEmpty() && !isRequired) {
                control.increment(0)
            } else if (new.isNotEmpty() && new != "-") {
                try {
                    control.increment(0); // won't change value, but will commit editor
                } catch (e: NumberFormatException) {
                    control.editor.text = control.valueFactory.value.toString()
                }
            }
        }
        control.focusedProperty().addListener { _, _, new ->
            if (!new && ((control.editor.text.isEmpty() && isRequired) || control.editor.text == "-")) {
                control.editor.text = control.valueFactory.value?.toString() ?: ""
            }

        }

        if (!isRequired) {
            node.value.action = HBox().apply {

                children += Button("⟲").apply {
                    tooltip = Tooltip("Reset to default")
                    onAction = EventHandler {
                        resetValueToDefault()
                    }
                }

                children += Button("Ø").apply {
                    tooltip = Tooltip("Set to NULL")
                    onAction = EventHandler {
                        setValueToNull()
                    }
                }
            }
        }
    }

    class IntegerSpinnerValueFactoryNullSafe(min: Int, max: Int) : SpinnerValueFactory<Int?>() {
        init {
            converter = IntegerStringConverter()

            valueProperty().addListener(ChangeListener { _, _, newValue: Int? ->
                if (newValue != null) {
                    if (newValue < min) {
                        value = min
                    } else if (newValue > max) {
                        value = max
                    }
                }
            })
        }

        override fun increment(steps: Int) {
            value = value?.plus(steps)
        }

        override fun decrement(steps: Int) {
            value = value?.minus(steps)
        }
    }
}