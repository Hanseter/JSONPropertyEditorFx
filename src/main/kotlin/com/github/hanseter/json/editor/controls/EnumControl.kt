package com.github.hanseter.json.editor.controls

import org.everit.json.schema.Schema
import org.controlsfx.control.ToggleSwitch
import javafx.beans.value.ChangeListener
import org.json.JSONObject
import javafx.beans.value.ObservableValue
import javafx.beans.property.Property
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import org.everit.json.schema.EnumSchema
import javafx.scene.control.ComboBox
import javafx.beans.property.SimpleObjectProperty

//TODO this control makes every enum a string, even if it is something else. This needs to be improved.
class EnumControl(schema: SchemaWrapper<Schema>, enumSchema: EnumSchema) :
	RowBasedControl<Schema, String, ComboBox<String>>(
		schema,
		ComboBox<String>(),
		SimpleObjectProperty<String>(""),
		schema.schema.getDefaultValue() as? String ?: enumSchema.getPossibleValuesAsList().first().toString()
	) {

	init {
		control.getItems().setAll(enumSchema.possibleValuesAsList.map { it.toString() })
		control.selectionModel.selectedIndexProperty()
			.addListener { _, _, new -> value.setValue(enumSchema.possibleValuesAsList.get(new.toInt()).toString()) }
		valueNewlyBound()
	}


	protected override fun valueNewlyBound() {
		if (control.items.contains(value.getValue())) {
			control.selectionModel.select(value.getValue())
		} else {
			control.selectionModel.select(0);
		}
	}
}