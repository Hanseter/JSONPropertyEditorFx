package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.actions.ActionsContainer
import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableBooleanValue
import org.controlsfx.control.ToggleSwitch
import org.everit.json.schema.BooleanSchema

class BooleanControl(override val schema: SchemaWrapper<BooleanSchema>, context: EditorContext) : TypeControl, ControlProvider<Boolean> {
    override val control = ToggleSwitch()
    override val value: Property<Boolean?>
        get() = control.selectedProperty()
    override val defaultValue: Boolean?
        get() = schema.schema.defaultValue as? Boolean
    override val editorActionsContainer: ActionsContainer = context.createActionContainer(this)

    private val delegate = RowBasedControl(this)

    override val node: FilterableTreeItem<TreeItemData> = delegate.node
    override val valid: ObservableBooleanValue = SimpleBooleanProperty(true)

    override fun bindTo(type: BindableJsonType) {
        delegate.bindTo(type)
        control.text = if (delegate.isBoundToNull()) TypeControl.NULL_PROMPT else ""
    }

}

