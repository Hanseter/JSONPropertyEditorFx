package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.util.BindableJsonType
import javafx.beans.property.SimpleBooleanProperty

class UnsupportedTypeControl(override val schema: SchemaWrapper<*>) : TypeControl {
    override val valid = SimpleBooleanProperty(true)
    override val node = FilterableTreeItem(TreeItemData("!!Error", "Schema ${schema.schema.schemaLocation} with type ${schema.schema::class.java.name} cannot be displayed.", null, null))
    override fun bindTo(type: BindableJsonType) {}
    override fun setBoundValue(newVal: Any?) {}
    override fun getBoundValue(): Any? = null
}