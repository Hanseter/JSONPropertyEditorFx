package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.actions.EditorAction
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonType
import org.controlsfx.control.ToggleSwitch
import org.everit.json.schema.BooleanSchema

class BooleanControl(schema: SchemaWrapper<BooleanSchema>, actions: List<EditorAction>) :
        RowBasedControl<BooleanSchema, Boolean, ToggleSwitch>(
                schema,
                ToggleSwitch(),
                { it.selectedProperty() },
                schema.schema.defaultValue as? Boolean,
                actions
        ) {

    override fun bindTo(type: BindableJsonType) {
        super.bindTo(type)

        control.text = if (isBoundToNull()) TypeControl.NULL_PROMPT else ""
    }

}

