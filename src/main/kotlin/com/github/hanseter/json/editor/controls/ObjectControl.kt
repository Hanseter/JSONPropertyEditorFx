package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.TreeItemData

interface ObjectControl :TypeControl{
    val requiredChildren: List<TypeControl>
    val optionalChildren: List<TypeControl>
}

fun createRequiredHeader() = FilterableTreeItem(TreeItemData("Required", null, null, null, isRoot = false, isHeadline = true))

fun createOptionalHeader() = FilterableTreeItem(TreeItemData("Optional", null, null, null, isRoot = false, isHeadline = true))

fun addRequiredAndOptionalChildren(node: FilterableTreeItem<TreeItemData>, required: List<TypeControl>, optional: List<TypeControl>) {
    if (required.isNotEmpty()) {
        node.add(createRequiredHeader())
        node.addAll(required.map { it.node })
    }

    if (optional.isNotEmpty()) {
        node.add(createOptionalHeader())
        node.addAll(optional.map { it.node })
    }
}