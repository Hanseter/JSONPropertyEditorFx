package com.github.hanseter.json.editor.extensions

import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.scene.Node
import javafx.scene.control.TreeItem
import java.util.function.Predicate

/**
 * Creates a filterable TreeItem with children.
 *
 * @param value value of the filterable TreeItem
 */

open class FilterableTreeItem<T>(value: T) : TreeItem<T>(value) {
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
class TreeItemData(val key: String, val description: String?, val control: Node?,
                   var action: Node?, val isRoot: Boolean = false, val isHeadline: Boolean = false)