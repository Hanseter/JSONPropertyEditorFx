package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.actions.EditorAction
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonType
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.ComboBox
import org.everit.json.schema.EnumSchema
import org.everit.json.schema.Schema

//TODO this control makes every enum a string, even if it is something else. This needs to be improved.
class EnumControl(schema: SchemaWrapper<Schema>, enumSchema: EnumSchema, actions: List<EditorAction>) :
        RowBasedControl<Schema, String, ComboBox<String>>(
                schema,
                ComboBox(),
                SimpleObjectProperty<String>(""),
                schema.schema.defaultValue as? String,
                actions
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

    override fun bindTo(type: BindableJsonType) {
        super.bindTo(type)
        control.promptText = if (isBoundToNull()) TypeControl.NULL_PROMPT else ""
    }
}