package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.types.TypeModel
import com.github.hanseter.json.editor.util.BindableJsonType
import javafx.beans.property.SimpleBooleanProperty

class UnsupportedTypeControl(override val model: TypeModel<Any?>) : TypeControl {
    override val valid = SimpleBooleanProperty(true)
    override val node = FilterableTreeItem(TreeItemData("!!Error", "Schema ${model.schema.schema.schemaLocation} with type ${model.schema.schema::class.java.name} cannot be displayed.", null, null))
    override fun bindTo(type: BindableJsonType) {}
}