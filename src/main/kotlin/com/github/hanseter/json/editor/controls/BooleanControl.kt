package com.github.hanseter.json.editor.controls

import org.everit.json.schema.BooleanSchema
import org.controlsfx.control.ToggleSwitch
import javafx.beans.value.ChangeListener
import org.json.JSONObject
import javafx.beans.value.ObservableValue
import javafx.beans.property.Property
import com.github.hanseter.json.editor.extensions.SchemaWrapper

class BooleanControl(schema: SchemaWrapper<BooleanSchema>) :
	RowBasedControl<BooleanSchema, Boolean, ToggleSwitch>(
		schema,
		ToggleSwitch(),
		{ it.selectedProperty() },
		schema.schema.getDefaultValue() as? Boolean 
	) {
}