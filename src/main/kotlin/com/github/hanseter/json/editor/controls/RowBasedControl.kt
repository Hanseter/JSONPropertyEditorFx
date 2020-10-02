package com.github.hanseter.json.editor.controls

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
        action: Node? = null
) : TypeControl, ChangeListener<TYPE?> {
    override val node = FilterableTreeItem(TreeItemData(schema.title, schema.schema.description, control, action, false))
    private var bound: BindableJsonType? = null
    override val valid = SimpleBooleanProperty(true)

    protected val isRequired = (schema.parent?.schema as? ObjectSchema)?.let {
        schema.getPropertyName() in it.requiredProperties
    } ?: true

    init {
        control.isDisable = schema.readOnly
        value.value = defaultValue
        value.addListener(this)
    }

    constructor(
            schema: SchemaWrapper<S>,
            control: C,
            propExtractor: (C) -> Property<TYPE?>,
            defaultValue: TYPE?
    ) : this(
            schema,
            control,
            propExtractor(control),
            defaultValue
    )

    override fun changed(observable: ObservableValue<out TYPE?>, oldValue: TYPE?, newValue: TYPE?) {
        bound?.setValue(schema, newValue)
    }

    @Suppress("UNCHECKED_CAST")
    override fun bindTo(type: BindableJsonType) {
        bound = null
        val rawVal = type.getValue(schema)
        var newVal: TYPE?

        control.styleClass.removeAll("at-default-value", "has-undefined-value", "has-null-value")

        if (rawVal == JSONObject.NULL) {
            newVal = null
            if ("has-null-value" !in control.styleClass) {
                control.styleClass.add("has-null-value")
            }
        } else {
            newVal = rawVal as? TYPE
            if (newVal == null) {
                if (defaultValue != null) {
                    newVal = defaultValue

                    if ("at-default-value" !in control.styleClass) {
                        control.styleClass.add("at-default-value")
                    }
                } else {
                    if ("has-undefined-value" !in control.styleClass) {
                        control.styleClass.add("has-undefined-value")
                    }
                }
            }
        }

        if (newVal != this.value.value) {
            this.value.value = newVal
            valueNewlyBound()
        }

        bound = type
    }

    protected open fun setValueToNull() {
        bound?.setValue(schema, JSONObject.NULL)
        valueNewlyBound()
    }

    protected open fun resetValueToDefault() {
        bound?.setValue(schema, null)
        valueNewlyBound()
    }

    protected open fun valueNewlyBound() {}
}