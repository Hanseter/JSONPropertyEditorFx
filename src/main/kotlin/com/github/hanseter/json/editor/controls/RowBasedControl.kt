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
import org.everit.json.schema.Schema


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
        var newVal = type.getValue(schema) as? TYPE
        if (newVal == null) {
            newVal = defaultValue
        }
        if (newVal != this.value.getValue()) {
            this.value.setValue(newVal)
            valueNewlyBound()
        }
        bound = type
    }

    protected open fun valueNewlyBound() {}
}