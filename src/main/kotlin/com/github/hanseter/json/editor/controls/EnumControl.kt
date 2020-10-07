package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.actions.ActionsContainer
import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.scene.control.ComboBox
import org.everit.json.schema.EnumSchema
import org.everit.json.schema.Schema

//TODO this control makes every enum a string, even if it is something else. This needs to be improved.
class EnumControl(override val schema: SchemaWrapper<Schema>, enumSchema: EnumSchema, context: EditorContext) : TypeControl, ControlProvider<String> {
    override val control = ComboBox<String>()
    override val value: Property<String?> = SimpleObjectProperty<String?>("")
    override val defaultValue: String?
        get() = schema.schema.defaultValue as? String
    override val editorActionsContainer: ActionsContainer = context.createActionContainer(this)

    private val delegate = RowBasedControl(this)

    override val node: FilterableTreeItem<TreeItemData> = delegate.node
    override val valid: ObservableBooleanValue = SimpleBooleanProperty(true)

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

    override fun bindTo(type: BindableJsonType) {
        if (delegate.bindTo(type)) {
            valueNewlyBound()
        }
        control.promptText = if (delegate.isBoundToNull()) TypeControl.NULL_PROMPT else ""
    }

    private fun valueNewlyBound() {
        if (control.items.contains(value.value)) {
            control.selectionModel.select(value.value)
        } else if (!delegate.isRequired && value.value == null) {
            control.selectionModel.select(null)
        } else {
            control.selectionModel.select(defaultValue);
        }
    }

}