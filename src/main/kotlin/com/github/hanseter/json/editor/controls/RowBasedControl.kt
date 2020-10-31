package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.actions.ActionsContainer
import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.types.ModelControlSynchronizer
import com.github.hanseter.json.editor.types.TypeModel
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import javafx.beans.property.SimpleBooleanProperty
import org.json.JSONObject

class RowBasedControl<T>(
        private val controlWithProperty: ControlWithProperty<T>,
        override val model: TypeModel<T?>,
        context: EditorContext) : TypeControl {

    private val editorActionsContainer: ActionsContainer = context.createActionContainer(this)
    override val node: FilterableTreeItem<TreeItemData> =
            FilterableTreeItem(TreeItemData(model.schema.title, model.schema.schema.description, controlWithProperty.control, editorActionsContainer))
    override val valid = SimpleBooleanProperty(model.validationErrors.isEmpty())
    private val synchronizer = ModelControlSynchronizer(controlWithProperty.property, model)

    override fun bindTo(type: BindableJsonType) {
        model.bound = type
        valid.set(model.validationErrors.isEmpty())
        val rawVal = type.getValue(model.schema)
        controlWithProperty.previewNull(isBoundToNull(rawVal))
        synchronizer.modelChanged()
        updateStyleClasses(rawVal)
        editorActionsContainer.updateDisablement()
    }

    private fun updateStyleClasses(rawVal: Any?) {
        controlWithProperty.control.styleClass.removeAll("has-null-value", "has-default-value")

        if (rawVal == JSONObject.NULL) {
            if ("has-null-value" !in controlWithProperty.control.styleClass) {
                controlWithProperty.control.styleClass += "has-null-value"
            }
        } else if (rawVal == null) {
            if (model.defaultValue != null) {
                if ("has-default-value" !in controlWithProperty.control.styleClass) {
                    controlWithProperty.control.styleClass += "has-default-value"
                }
            } else {
                if ("has-null-value" !in controlWithProperty.control.styleClass) {
                    controlWithProperty.control.styleClass += "has-null-value"
                }
            }
        }
    }

    private fun isBoundToNull(rawVal: Any?): Boolean = !isBoundToDefault(rawVal) && JSONObject.NULL == rawVal

    private fun isBoundToDefault(rawVal: Any?): Boolean = model.defaultValue != null && null == rawVal
}