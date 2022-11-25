/*
 * SWEETS - Software Engineering Tooling Suite
 *
 * Copyright (c) Siemens Mobility GmbH 2022, All Rights Reserved, Confidential.
 */
package com.github.hanseter.json.editor.ui

import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent

/**
 *
 * @author Henrik Frühling (henrik.fruehling@siemens.com)
 */
class CustomTreeTableView<S> : TreeTableView<S>() {
    init {
        addEventHandler(KeyEvent.KEY_PRESSED) {
            if (!(it.isAltDown && it.code.isArrowKey)) {
                it.consume()
            }
        }
        addEventFilter(KeyEvent.KEY_PRESSED) {
            if (it.isConsumed) return@addEventFilter

            if (it.code == KeyCode.TAB) {
                it.consume()
                selectNext()
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

    private fun getSelected() = this.selectionModel.selectedCells.firstOrNull()?.treeItem
    private fun expandSelectedCell() = getSelected()?.also {
        if (!it.isLeaf) {
            it.setExpanedTreeItem(true)
        }
    }

    private fun collapseSelectedCell() = getSelected()?.also {
        if (it.isLeaf) {
            it.parent?.also { parent ->
                parent.setExpanedTreeItem(false)
            }
        } else {
            it.setExpanedTreeItem(false)
        }
    }

    companion object {
        private fun CustomTreeTableView<*>.selectNext() {
            val selectedRow = selectionModel.selectedCells.firstOrNull()?.row ?: -1
            val lastRowIndex = expandedItemCount - 1
            when {
                selectedRow < 0 -> selectFirst()
                selectedRow < lastRowIndex -> selectDown()
                selectedRow == lastRowIndex -> selectFirst()
            }
        }

        private fun CustomTreeTableView<*>.scrollToCurrentRow() {
            scrollTo(selectionModel.selectedIndex)
        }

        private fun CustomTreeTableView<*>.selectFirst() {
            selectionModel.selectFirst()
            scrollToCurrentRow()
        }


        private fun CustomTreeTableView<*>.selectUp() {
            selectionModel.selectAboveCell()
            scrollToCurrentRow()
        }

        private fun CustomTreeTableView<*>.selectDown() {
            selectionModel.selectBelowCell()
            scrollToCurrentRow()
        }

        private fun CustomTreeTableView<*>.selectLeft() {
            selectionModel.selectLeftCell()
        }

        private fun CustomTreeTableView<*>.selectRight() {
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
}