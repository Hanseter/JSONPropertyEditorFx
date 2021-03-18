package com.github.hanseter.json.editor.ui

/**
 * The state of the UI, to be stashed and reapplied whenever the schema changes.
 */
data class UiState(val rowState: RowUiState)

data class RowUiState(val title: String, val expanded: Boolean, val children: Map<String, RowUiState> = emptyMap()) {

    constructor(item: FilterableTreeItem<TreeItemData>) : this(item.value.title, item.isExpanded, item.list.associateBy({ it.value.title }, { RowUiState(it) }))

    fun matches(item: FilterableTreeItem<TreeItemData>) = title == item.value.title

    fun apply(item: FilterableTreeItem<TreeItemData>) {
        if (matches(item)) {

            item.isExpanded = expanded

            item.list.forEach {
                children[it.value.title]?.apply(it)
            }
        }
    }
}