package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import org.controlsfx.control.ToggleSwitch
import org.everit.json.schema.BooleanSchema

class BooleanControl(schema: SchemaWrapper<BooleanSchema>) :
	RowBasedControl<BooleanSchema, Boolean, ToggleSwitch>(
		schema,
		ToggleSwitch(),
		{ it.selectedProperty() },
		schema.schema.defaultValue as? Boolean
	)