package com.github.hanseter.json.editor.controls

import javafx.scene.Node
import javafx.scene.control.TreeTableCell
import javafx.scene.control.TreeTableColumn
import javafx.util.Callback

class NodeTreeTableCell<S, T>(private val nodeGetter: (T) -> Node?) : TreeTableCell<S, T>() {

    init {
        graphic = null
    }


    override fun updateItem(item: T?, empty: Boolean) {
        super.updateItem(item, empty)

        val node = item?.let(nodeGetter)

        if (node == null || empty) {
            text = null
            graphic = null
        } else {
            if (node.parent == null) {
                graphic = node
            } else {
                // when auto-resizing a column, the cells are temporarily duplicated to measure their width
                // this prevents the duplicates from stealing the graphic

                val parent = node as? TreeTableCell<*, *>
                if (parent != this && parent?.index == index) {
                    text = "Workaround"
                    prefWidth = node.prefWidth(-1.0)
                }
            }
        }


    }

    companion object {

        fun <S, T> forColumn(getter: (T) -> Node?): Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> {
            return Callback {
                NodeTreeTableCell(getter)
            }
        }

    }

}