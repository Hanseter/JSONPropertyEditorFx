package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import org.everit.json.schema.EnumSchema
import org.everit.json.schema.Schema

//TODO this control makes every enum a string, even if it is something else. This needs to be improved.
class EnumControl(schema: SchemaWrapper<Schema>, enumSchema: EnumSchema) :
        RowBasedControl<Schema, String, ComboBox<String>>(
                schema,
                ComboBox(),
                SimpleObjectProperty<String>(""),
                schema.schema.defaultValue as? String
        ) {

    init {
        control.minWidth = 150.0
        control.items.setAll(enumSchema.possibleValuesAsList.map { it.toString() })
        control.selectionModel.selectedIndexProperty()
                .addListener { _, _, new ->
                    if (new.toInt() >= 0) {
                        value.setValue(enumSchema.possibleValuesAsList[new.toInt()].toString())
                    }
                }
        valueNewlyBound()

        if (!isRequired) {
            node.value.action = HBox().apply {

                if (defaultValue != null) {
                    children += Button("⟲").apply {
                        tooltip = Tooltip("Reset to default")
                        onAction = EventHandler {
                            resetValueToDefault()
                        }
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

    override fun valueNewlyBound() {
        if (control.items.contains(value.value)) {
            control.selectionModel.select(value.value)
        } else if (!isRequired && value.value == null) {
            control.selectionModel.select(null)
        } else {
            control.selectionModel.select(defaultValue);
        }
    }
}