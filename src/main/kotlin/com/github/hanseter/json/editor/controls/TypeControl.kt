package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.util.BindableJsonType
import javafx.beans.value.ObservableBooleanValue

interface TypeControl {
    val schema: SchemaWrapper<*>
    val node: FilterableTreeItem<TreeItemData>
    val valid: ObservableBooleanValue

    fun bindTo(type: BindableJsonType)

    fun getBoundValue(): Any?

    fun setBoundValue(newVal: Any?)

    companion object {
        const val NULL_PROMPT = "Null"
    }
}