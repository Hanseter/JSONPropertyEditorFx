package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.actions.EditorAction
import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.util.BindableJsonType
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.control.Control
import org.everit.json.schema.ObjectSchema
import org.everit.json.schema.Schema
import org.json.JSONObject


abstract class RowBasedControl<S : Schema, TYPE : Any, C : Control>(
        final override val schema: SchemaWrapper<S>,
        protected val control: C,
        protected val value: Property<TYPE?>,
        protected val defaultValue: TYPE?,
        protected val actions: List<EditorAction>,
        action: Node? = null
) : TypeControl, ChangeListener<TYPE?> {
    override val node = FilterableTreeItem(TreeItemData(schema.title, schema.schema.description, control, action, false))
    private var bound: BindableJsonType? = null
    override val valid = SimpleBooleanProperty(true)

    protected val editorActionsContainer = ActionsContainer.forActions(this, schema, actions)

    protected val isRequired = (schema.parent?.schema as? ObjectSchema)?.let {
        schema.getPropertyName() in it.requiredProperties
    } ?: true

    init {
        control.isDisable = schema.readOnly
        value.value = defaultValue
        value.addListener(this)

        node.value.action = editorActionsContainer
    }

    constructor(
            schema: SchemaWrapper<S>,
            control: C,
            propExtractor: (C) -> Property<TYPE?>,
            defaultValue: TYPE?,
            actions: List<EditorAction>
    ) : this(
            schema,
            control,
            propExtractor(control),
            defaultValue,
            actions
    )

    override fun changed(observable: ObservableValue<out TYPE?>, oldValue: TYPE?, newValue: TYPE?) {
        bound?.setValue(schema, newValue)
    }

    @Suppress("UNCHECKED_CAST")
    override fun bindTo(type: BindableJsonType) {
        bound = null
        val rawVal = type.getValue(schema)
        val newVal: TYPE?

        control.styleClass.removeAll("has-null-value")

        newVal = when (rawVal) {
            null -> {
                defaultValue
            }
            JSONObject.NULL -> {
                null
            }
            else -> {
                rawVal as? TYPE
            }
        }

        if (newVal == null) {
            if ("has-null-value" !in control.styleClass) {
                control.styleClass.add("has-null-value")
            }
        } else {
            control.styleClass.remove("has-null-value")
        }

        if (newVal != this.value.value) {
            this.value.value = newVal
            valueNewlyBound()
        }

        bound = type

        editorActionsContainer.updateDisablement()
    }

    override fun setBoundValue(newVal: Any?) {
        bound?.setValue(schema, newVal)
        valueNewlyBound()
    }

    override fun getBoundValue(): Any? {
        return bound?.getValue(schema)
    }

    protected fun isBoundToNull(): Boolean {
        return JSONObject.NULL == getBoundValue()
    }

    protected open fun valueNewlyBound() {}
}