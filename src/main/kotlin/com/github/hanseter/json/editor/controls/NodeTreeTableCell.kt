package com.github.hanseter.json.editor.controls

import javafx.scene.Node
import javafx.scene.control.TreeTableCell
import javafx.scene.control.TreeTableColumn
import javafx.util.Callback
import org.controlsfx.control.ToggleSwitch

class NodeTreeTableCell<S, T>(private val nodeGetter: (T) -> Node?) : TreeTableCell<S, T>() {

    val switch = ToggleSwitch()

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