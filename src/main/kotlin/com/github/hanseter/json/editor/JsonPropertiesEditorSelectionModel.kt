package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.ui.ControlTreeItemData
import com.github.hanseter.json.editor.ui.TreeItemData
import javafx.scene.control.SingleSelectionModel
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableView

abstract class JsonPropertiesEditorSelectionModel : SingleSelectionModel<ElementField>() {

    /**
     * The index of the first visible control, `-1` if there is none.
     */
    abstract val indexOfFirstVisibleControl: Int

    /**
     * The index of the first control, `-1` if there is none.
     */
    abstract val indexOfFirstControl: Int

    /**
     * The index of the last visible control, `-1` if there is none.
     */
    abstract val indexOfLastVisibleControl: Int

    /**
     * The index of the last control, `-1` if there is none.
     */
    abstract val indexOfLastControl: Int

    /**
     * Finds the index of the first visible control of the element.
     * -1 if there is no such element or there are no (visible) controls.
     */
    abstract fun indexOfFirstVisibleControl(elementId: String): Int

    /**
     * Finds the index of the first control of the element.
     * -1 if there is no such element or there are no controls.
     */
    abstract fun indexOfFirstControl(elementId: String): Int

    /**
     * Finds the index of the last visible control of the element.
     * -1 if there is no such element or there are no (visible) controls.
     */
    abstract fun indexOfLastVisibleControl(elementId: String): Int

    /**
     * Finds the index of the last control of the element.
     * -1 if there is no such element or there are no controls.
     */
    abstract fun indexOfLastControl(elementId: String): Int

    /**
     * Finds the index of the [field].
     */
    abstract fun findIndexOfField(field: ElementField): Int

    /**
     * Finds the index of the closest parent of [field] in the tree that is still visible
     */
    abstract fun findIndexOfVisibleParent(field: ElementField): Int

    class JsonPropertiesEditorSelectionModelImpl(
        private val editor: JsonPropertiesEditor,
        private val panes: Map<String, JsonPropertiesPane>,
        private val treeTable: TreeTableView<TreeItemData>
    ) : JsonPropertiesEditorSelectionModel() {

        override val indexOfFirstVisibleControl: Int
            get() = treeTable.root.visibleDescendants.indexOfFirst { it.value is ControlTreeItemData }
        override val indexOfFirstControl: Int
            get() = treeTable.root.descendants.indexOfFirst { it.value is ControlTreeItemData }
        override val indexOfLastVisibleControl: Int
            get() = treeTable.root.visibleDescendants.indexOfLast { it.value is ControlTreeItemData }
        override val indexOfLastControl: Int
            get() = treeTable.root.descendants.indexOfLast { it.value is ControlTreeItemData }

        override fun indexOfFirstVisibleControl(elementId: String): Int {
            val (index, item) = findIndexAndItemOfPane(elementId) ?: return -1
            val controlIndex =
                item.visibleDescendants.indexOfFirst { it.value is ControlTreeItemData }
            return if (controlIndex == -1) -1
            else index + controlIndex + 1
        }

        override fun indexOfFirstControl(elementId: String): Int {
            val (index, item) = findIndexAndItemOfPane(elementId) ?: return -1
            val controlIndex = item.descendants.indexOfFirst { it.value is ControlTreeItemData }
            return if (controlIndex == -1) -1
            else index + controlIndex + 1
        }

        override fun indexOfLastVisibleControl(elementId: String): Int {
            val (index, item) = findIndexAndItemOfPane(elementId) ?: return -1
            val controlIndex =
                item.visibleDescendants.indexOfLast { it.value is ControlTreeItemData }
            return if (controlIndex == -1) -1
            else index + controlIndex + 1
        }

        override fun indexOfLastControl(elementId: String): Int {
            val (index, item) = findIndexAndItemOfPane(elementId) ?: return -1
            val controlIndex = item.descendants.indexOfLast { it.value is ControlTreeItemData }
            return if (controlIndex == -1) -1
            else index + controlIndex + 1
        }

        override fun select(index: Int) {
            super.select(index)
            if (index == -1) return
            val item = treeTable.root.descendants.drop(index).firstOrNull() ?: return
            selectInTree(item)
        }

        private fun selectInTree(item: TreeItem<TreeItemData>) {
            val index = treeTable.getRow(item)
            if (index == -1) return
            treeTable.selectionModel.select(index, treeTable.columns[1])
        }

        override fun select(obj: ElementField?) {
            if (obj == null) {
                selectedIndex = -1
                selectedItem = null
                return
            }

            selectedItem = obj

            val (index, item) = findIndexAndItem(obj) ?: return
            selectedIndex = index
            selectInTree(item)
        }

        override fun getModelItem(index: Int): ElementField? {
            val item = treeTable.root.descendants.drop(index).firstOrNull() ?: return null
            val pointer =
                (item.value as? ControlTreeItemData)?.typeControl?.model?.schema?.pointer ?: return null
            val paneItem = item.pathFromRoot[1]
            val pane = panes.entries.find { it.value.treeItem == paneItem } ?: return null

            return ElementField(pane.key, pointer)
        }

        override fun findIndexOfField(field: ElementField): Int {
            val paneItem = panes[field.elementId]?.treeItem ?: return -1
            val paneIndex = treeTable.root.visibleDescendants.indexOf(paneItem)
            if (paneIndex == -1) return -1

            val fieldIndex = paneItem.descendants.indexOfFirst {
                (it.value as? ControlTreeItemData)?.typeControl?.model?.schema?.pointer == field.fieldPointer
            }
            if (fieldIndex == -1) return -1
            return paneIndex + fieldIndex + 1
        }

        private fun findIndexAndItem(field: ElementField): IndexedValue<TreeItem<TreeItemData>>? {
            val paneItem = panes[field.elementId]?.treeItem ?: return null
            val paneIndex = treeTable.root.visibleDescendants.indexOf(paneItem)
            if (paneIndex == -1) return null

            return paneItem.descendants.withIndex().firstOrNull {
                (it.value.value as? ControlTreeItemData)?.typeControl?.model?.schema?.pointer == field.fieldPointer
            }
        }

        override fun isEmpty(): Boolean = treeTable.root.children.isEmpty()

        override fun getItemCount(): Int =
            treeTable.root.descendants.count()

        override fun findIndexOfVisibleParent(field: ElementField): Int {
            val paneItem = panes[field.elementId]?.treeItem ?: return -1
            val paneIndex = treeTable.root.visibleDescendants.indexOf(paneItem)
            if (paneIndex == -1) return -1

            val fieldItem = paneItem.descendants.find {
                (it.value as? ControlTreeItemData)?.typeControl?.model?.schema?.pointer == field.fieldPointer
            } ?: return -1

            val visibleParent = fieldItem.pathFromRoot.find { !it.isExpanded } ?: fieldItem

            val fieldIndex = if (visibleParent == paneItem) -1
            else paneItem.visibleDescendants.indexOf(visibleParent).also { if (it == -1) return -1 }
            return paneIndex + fieldIndex + 1
        }

        fun findIndexAndItemOfPane(id: String): IndexedValue<TreeItem<TreeItemData>>? {
            val item = panes[id]?.treeItem ?: return null
            val index = treeTable.root.visibleDescendants.indexOf(item)
            return if (index == -1) null
            else IndexedValue(index, item)
        }

    }

    companion object {
        private val <T> TreeItem<T>.descendants: Sequence<TreeItem<T>>
            get() = children.asSequence().flatMap { sequenceOf(it) + it.descendants }

        private val <T> TreeItem<T>.visibleDescendants: Sequence<TreeItem<T>>
            get() = if (isExpanded) children.asSequence()
                .flatMap { sequenceOf(it) + it.visibleDescendants }
            else emptySequence()

        private val <T> TreeItem<T>.ancestors: Sequence<TreeItem<T>>
            get() = generateSequence(parent) { it.parent }

        private val <T> TreeItem<T>.pathToRoot: Sequence<TreeItem<T>>
            get() = generateSequence(this) { it.parent }
        private val <T> TreeItem<T>.pathFromRoot: List<TreeItem<T>>
            get() = pathToRoot.toList().reversed()
    }

}