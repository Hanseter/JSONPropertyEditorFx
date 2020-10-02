package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonType
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import org.controlsfx.control.ToggleSwitch
import org.everit.json.schema.BooleanSchema
import org.everit.json.schema.ObjectSchema

class BooleanControl(schema: SchemaWrapper<BooleanSchema>) :
        RowBasedControl<BooleanSchema, Boolean, ToggleSwitch>(
                schema,
                ToggleSwitch(),
                { it.selectedProperty() },
                schema.schema.defaultValue as? Boolean
        ) {

    private val actions = HBox()

    init {
        node.value.action = actions

        val parentSchema = schema.parent?.schema
        if (parentSchema is ObjectSchema && schema.getPropertyName() !in parentSchema.requiredProperties) {

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
    }

    override fun bindTo(type: BindableJsonType) {
        super.bindTo(type)
    }

}
