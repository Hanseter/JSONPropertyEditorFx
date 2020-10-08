package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.actions.ActionsContainer
import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.util.BindableJsonType
import javafx.beans.property.Property
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.control.Control
import org.everit.json.schema.ObjectSchema
import org.json.JSONObject

interface ControlProvider<T> {
    val schema: SchemaWrapper<*>
    val control: Control
    val value: Property<out T?>
    val defaultValue: T?
    val editorActionsContainer: ActionsContainer
}

class RowBasedControl<T>(private val provider: ControlProvider<T>) : ChangeListener<Any?> {
    val node = FilterableTreeItem(TreeItemData(provider.schema.title, provider.schema.schema.description, provider.control, provider.editorActionsContainer))
    private var bound: BindableJsonType? = null

    val isRequired = (provider.schema.parent?.schema as? ObjectSchema)?.let {
        provider.schema.getPropertyName() in it.requiredProperties
    } ?: true

    init {
        provider.control.isDisable = provider.schema.readOnly
        provider.value.value = provider.defaultValue
        provider.value.addListener(this)
    }

    override fun changed(observable: ObservableValue<out Any?>, oldValue: Any?, newValue: Any?) {
        bound?.setValue(provider.schema, newValue)
    }

    /**
     * Returns whether the value actually changed
     */
    fun bindTo(type: BindableJsonType, converter: (Any) -> T?): Boolean {
        bound = null
        val rawVal = type.getValue(provider.schema)

        val toAssign = when (rawVal) {
            null -> provider.defaultValue
            JSONObject.NULL -> null
            else -> {
                val converted = converter(rawVal)
                if (converted == null) {
                    //TODO This should not happen with correct data and should be logged
                }
                converted
            }
        }

        val changed = toAssign != provider.value.value
        if (changed) {
            provider.value.value = toAssign
        }

        bound = type

        updateStyleClasses(rawVal)
        provider.editorActionsContainer.updateDisablement()
        return changed
    }

    private fun updateStyleClasses(rawVal: Any?) {
        provider.control.styleClass.removeAll("has-null-value", "has-default-value")

        if (rawVal == JSONObject.NULL) {
            if ("has-null-value" !in provider.control.styleClass) {
                provider.control.styleClass += "has-null-value"
            }
        } else if (rawVal == null) {
            if (provider.defaultValue != null) {
                if ("has-default-value" !in provider.control.styleClass) {
                    provider.control.styleClass += "has-default-value"
                }
            } else {
                if ("has-null-value" !in provider.control.styleClass) {
                    provider.control.styleClass += "has-null-value"
                }
            }
        }
    }

    fun getBoundValue(): Any? = bound?.getValue(provider.schema)

    fun isBoundToNull(): Boolean = !isBoundToDefault() && JSONObject.NULL == getBoundValue()

    fun isBoundToDefault(): Boolean = provider.defaultValue != null && null == getBoundValue()
}