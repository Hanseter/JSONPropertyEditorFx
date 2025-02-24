package com.github.hanseter.json.editor.ui

import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableView
import javafx.scene.control.skin.TreeTableViewSkin
import javafx.scene.control.skin.VirtualFlow
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent

object TreeTableNavigation {

    fun addNavigationToTreeTableView(treeTableView: TreeTableView<TreeItemData>) {
        treeTableView.apply {
            addEventFilter(KeyEvent.KEY_PRESSED) {
                if (it.isConsumed) return@addEventFilter

                if (it.code == KeyCode.TAB) {
                    it.consume()

                    if (it.isShiftDown) {
                        selectPrevious()
                    } else {
                        selectNext()
                    }
                }

                if (it.code.isArrowKey && it.isAltDown) {
                    it.consume()
                    when (it.code) {
                        in UP_KEYS -> selectUp()
                        in DOWN_KEYS -> selectDown()
                        in LEFT_KEYS -> selectLeft()
                        in RIGHT_KEYS -> selectRight()
                        else -> {}
                    }
                }
            }
            addEventFilter(KeyEvent.KEY_PRESSED) {
                if (it.isConsumed) return@addEventFilter
                if (it.code in FOLD_KEYS && it.isAltDown) {
                    it.consume()
                    when (it.code) {
                        in EXPAND_KEYS -> expandSelectedCell()
                        in COLLAPSE_KEYS -> collapseSelectedCell()
                        else -> {}
                    }
                }
            }
        }

    }

    private fun TreeTableView<*>.getVirtualFlow() = (this.skin as? TreeTableViewSkin<*>)?.children
        ?.filterIsInstance<VirtualFlow<*>>()
        ?.firstOrNull()

    private fun TreeTableView<*>.getSelected() =
        selectionModel.selectedCells.firstOrNull()?.treeItem

    private fun TreeTableView<*>.expandSelectedCell() = getSelected()?.also {
        if (!it.isLeaf) {
            it.setExpanedTreeItem(true)
        }
    }

    private fun TreeTableView<*>.collapseSelectedCell() = getSelected()?.also {
        if (it.isLeaf) {
            it.parent?.also { parent ->
                parent.setExpanedTreeItem(false)
            }
        } else {
            it.setExpanedTreeItem(false)
        }
    }

    private fun TreeTableView<*>.selectNext() {
        val selectedRow = selectionModel.selectedCells.firstOrNull()?.row ?: -1
        val lastRowIndex = expandedItemCount - 1
        when {
            selectedRow < 0 -> selectFirst()
            selectedRow < lastRowIndex -> selectDown()
            selectedRow == lastRowIndex -> selectFirst()
        }
    }

    private fun TreeTableView<*>.selectPrevious() {
        val selectedRow = selectionModel.selectedCells.firstOrNull()?.row ?: -1
        when {
            selectedRow > 0 -> selectUp()
            selectedRow == 0 -> selectLast()
            else -> selectFirst()
        }
    }

    private fun TreeTableView<*>.scrollToSelectedRow() {
        scrollToRow(selectionModel.selectedIndex)
    }

    private fun TreeTableView<*>.scrollToRow(rowIndex:Int) {
        getVirtualFlow()?.scrollTo(rowIndex)
    }

    private fun TreeTableView<*>.selectFirst() {
        selectionModel.selectFirst()
        scrollToSelectedRow()
    }

    private fun TreeTableView<*>.selectLast() {
        selectionModel.selectLast()
        scrollToSelectedRow()
    }


    private fun TreeTableView<*>.selectUp() {
        selectionModel.selectAboveCell()
        scrollToSelectedRow()
    }

    private fun TreeTableView<*>.selectDown() {
        selectionModel.selectBelowCell()
        scrollToSelectedRow()
    }

    private fun TreeTableView<*>.selectLeft() {
        selectionModel.selectLeftCell()
    }

    private fun TreeTableView<*>.selectRight() {
        selectionModel.selectRightCell()
    }

    private fun TreeItem<*>.setExpanedTreeItem(expanded: Boolean) {
        isExpanded = expanded
    }

    private val UP_KEYS = setOf(KeyCode.UP, KeyCode.KP_UP)
    private val DOWN_KEYS = setOf(KeyCode.DOWN, KeyCode.KP_DOWN)
    private val LEFT_KEYS = setOf(KeyCode.LEFT, KeyCode.KP_LEFT)
    private val RIGHT_KEYS = setOf(KeyCode.RIGHT, KeyCode.KP_RIGHT)
    private val EXPAND_KEYS = setOf(KeyCode.PLUS, KeyCode.ADD)
    private val COLLAPSE_KEYS = setOf(KeyCode.MINUS, KeyCode.SUBTRACT)
    private val FOLD_KEYS = EXPAND_KEYS + COLLAPSE_KEYS
}