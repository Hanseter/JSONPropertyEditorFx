package com.github.hanseter.json.editor.extensions

import com.github.hanseter.json.editor.actions.ActionsContainer
import com.github.hanseter.json.editor.controls.TypeControl
import com.github.hanseter.json.editor.util.DecoratableLabelSkin
import com.github.hanseter.json.editor.util.ViewOptions
import com.github.hanseter.json.editor.validators.isRequiredSchema
import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.control.TreeItem
import java.util.function.Predicate

/**
 * Creates a filterable TreeItem with children.
 *
 * @param value value of the filterable TreeItem
 */

class FilterableTreeItem<T>(value: T) : TreeItem<T>(value) {
    val list: ObservableList<FilterableTreeItem<T>> = FXCollections.observableArrayList()
    private val filteredList: FilteredList<FilterableTreeItem<T>> = FilteredList(list)

    init {
        Bindings.bindContent(super.getChildren(), filteredList)
    }

    /**
     * The children of this FilteredTreeItem. This method is called frequently, and
     * it is therefore recommended that the returned list be cached by
     * any TreeItem implementations.
     *
     * @return an unmodifiable list that contains the child TreeItems belonging to the TreeItem.
     */
    override fun getChildren(): ObservableList<TreeItem<T>> {
        return FXCollections.unmodifiableObservableList(super.getChildren())
    }

    /**
     * Set the predicate of the item and also its child items. Doesn't set the predicate, if there
     * the item has child items as only tree leads should be filtered.
     *
     * @param predicate the predicate
     */
    fun setPredicate(predicate: Predicate<T>) {
        filteredList.setPredicate { child ->
            child.setPredicate(predicate)

            if (child.children.size > 0) {
                true
            } else {
                predicate.test(child.value)
            }
        }
    }

    /**
     * Adds an filterable tree item to the source list which backs the filtered list.
     * @param item a filterable tree item
     */
    fun add(item: FilterableTreeItem<T>) {
        list.add(item)
    }

    /**
     * Adds multiple filterable tree items to the source list which backs the filtered list.
     * @param items a list of filterable tree items
     */
    fun addAll(vararg items: FilterableTreeItem<T>) {
        list.addAll(items)
    }

    /**
     * Adds multiple filterable tree items to the source list which backs the filtered list.
     * @param items a collection of filterable tree items
     */
    fun addAll(items: Collection<FilterableTreeItem<T>>) {
        list.addAll(items)
    }

    /**
     * Removes a filterable tree item from the source list.
     * @param item item to be removed
     */
    fun remove(item: FilterableTreeItem<T>) {
        list.remove(item)
    }

    /**
     * Clears the source list.
     */
    fun clear() {
        list.clear()
    }
}

/**
 * TreeItem Data class which holds a key, description, control and action of a TreeItem.
 * By default it's not treated as a root item.
 *
 */
interface TreeItemData {
    val label: Label
    val title: String
    val control: Node?
    val actions: ActionsContainer?
    val isRoot: Boolean
        get() = false
    val isHeadline: Boolean
        get() = false
}

class ControlTreeItemData(
        val typeControl: TypeControl,
        override val actions: ActionsContainer,
        val validators: List<com.github.hanseter.json.editor.validators.Validator>,
        viewOptions: ViewOptions) : TreeItemData {

    override val title = typeControl.model.schema.title

    override val label = Label(
            title + if (viewOptions.markRequired && isRequiredSchema(typeControl.model.schema)) " *" else ""
    ).apply {
        tooltip = typeControl.model.schema.schema.description?.let { Tooltip(it) }
        skin = DecoratableLabelSkin(this)
    }

    override val control: Node?
        get() = typeControl.control
}

object RootTreeItemData : TreeItemData {
    override val title = "root"

    override val label: Label = Label(title)

    override val control: Node?
        get() = null
    override val actions: ActionsContainer?
        get() = null
}

class SectionRootTreeItemData(override val title: String) : TreeItemData {
    override val label: Label = Label(title)
    override val control: Node?
        get() = null
    override val actions: ActionsContainer?
        get() = null
    override val isRoot: Boolean
        get() = true
}

class HeaderTreeItemData(override val title: String) : TreeItemData {
    override val label: Label = Label(title)
    override val control: Node?
        get() = null
    override val actions: ActionsContainer?
        get() = null
    override val isHeadline: Boolean
        get() = true
}