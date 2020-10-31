package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.types.TypeModel
import com.github.hanseter.json.editor.util.BindableJsonType
import javafx.beans.value.ObservableBooleanValue

interface TypeControl {
    val node: FilterableTreeItem<TreeItemData>
    val valid: ObservableBooleanValue
    val model: TypeModel<*>

    fun bindTo(type: BindableJsonType)

    companion object {
        const val NULL_PROMPT = "Null"
    }
}