package com.github.hanseter.json.editor.ui

import com.github.hanseter.json.editor.actions.ActionsContainer
import com.github.hanseter.json.editor.actions.EditorAction
import com.github.hanseter.json.editor.controls.TypeControl
import com.github.hanseter.json.editor.util.LazyControl
import com.github.hanseter.json.editor.validators.Validator
import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.event.Event
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
    val title: String
    val description: String?
    val required: Boolean
    val cssClasses: List<String>
    val cssStyle: String?
    var validationMessage: String?

    fun createControl(): LazyControl?

    fun createActions(): ActionsContainer?

    fun registerChangeListener(listener: (TreeItemData) -> Unit)
    fun removeChangeListener(listener: (TreeItemData) -> Unit)
    fun updateFinished()
}

class ControlTreeItemData(
        val typeControl: TypeControl,
        private val actions: List<EditorAction>,
        private val actionHandler: (Event, EditorAction, TypeControl) -> Unit,
        private val objId: String,
        val validators: List<Validator>) : TreeItemData {
    private val changeListeners: MutableList<(TreeItemData) -> Unit> = mutableListOf()

    override val title = typeControl.model.schema.title

    override val description: String?
        get() = typeControl.model.schema.baseSchema.description

    override val required: Boolean
        get() = typeControl.model.schema.required
    override val cssClasses: List<String>
        get() = emptyList()
    override val cssStyle: String?
        get() = typeControl.model.schema.cssStyle

    override var validationMessage: String? = null

    override fun createControl(): LazyControl? = typeControl.createLazyControl()

    override fun createActions(): ActionsContainer? = ActionsContainer(typeControl, actions, objId, actionHandler)

    override fun registerChangeListener(listener: (TreeItemData) -> Unit) {
        changeListeners.add(listener)
    }

    override fun removeChangeListener(listener: (TreeItemData) -> Unit) {
        changeListeners.remove(listener)
    }

    override fun updateFinished() {
        changeListeners.forEach { it(this) }
    }
}

class StyledTreeItemData(override val title: String, override val cssClasses: List<String>) : TreeItemData {
    private val changeListeners: MutableList<(TreeItemData) -> Unit> = mutableListOf()

    override val description: String?
        get() = null

    override val required: Boolean
        get() = false
    override var validationMessage: String? = null

    override val cssStyle: String?
        get() = null

    override fun createControl(): LazyControl? = null

    override fun createActions(): ActionsContainer? = null

    override fun registerChangeListener(listener: (TreeItemData) -> Unit) {
        changeListeners.forEach { it(this) }
    }

    override fun removeChangeListener(listener: (TreeItemData) -> Unit) {
        changeListeners.forEach { it(this) }
    }

    override fun updateFinished() {
        changeListeners.forEach { it(this) }
    }
}

