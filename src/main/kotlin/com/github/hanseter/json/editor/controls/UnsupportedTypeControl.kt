package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.types.SupportedType
import com.github.hanseter.json.editor.types.TypeModel
import com.github.hanseter.json.editor.util.BindableJsonType
import javafx.scene.Node
import javafx.scene.control.Label

class UnsupportedTypeControl(override val model: TypeModel<Any?, SupportedType.SimpleType.UnsupportedType>) : TypeControl {
    override val control: Node?
        get() = Label("!!Error: Schema ${model.schema.schema.schemaLocation} with type ${model.schema.schema::class.java.name} cannot be displayed.")
    override val childControls: List<TypeControl>
        get() = emptyList()
//    override val node = FilterableTreeItem(TreeItemData("!!Error", "Schema ${model.schema.schema.schemaLocation} with type ${model.schema.schema::class.java.name} cannot be displayed.", null, null))
    override fun bindTo(type: BindableJsonType) {}
}