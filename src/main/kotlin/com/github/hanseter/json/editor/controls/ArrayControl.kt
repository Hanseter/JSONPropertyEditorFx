package com.github.hanseter.json.editor.controls

import javafx.scene.layout.VBox
import org.everit.json.schema.ArraySchema
import org.everit.json.schema.Schema
import com.github.hanseter.json.editor.ControlFactory
import javafx.scene.control.TitledPane
import com.github.hanseter.json.editor.util.BindableJsonType
import org.json.JSONArray
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.event.EventHandler
import javafx.scene.control.SeparatorMenuItem
import com.github.hanseter.json.editor.util.BindableJsonObject
import org.json.JSONObject
import com.github.hanseter.json.editor.util.BindableJsonArray
import com.github.hanseter.json.editor.util.BindableJsonArrayEntry
import com.github.hanseter.json.editor.IdReferenceProposalProvider
import com.github.hanseter.json.editor.extensions.SchemaWrapper

class ArrayControl(
	override val schema: SchemaWrapper<ArraySchema>,
	private val contentSchema: Schema,
	private val refProvider: IdReferenceProposalProvider
) :
	TypeWithChildrenControl(schema, listOf(), refProvider) {

	private var bound: BindableJsonType? = null
	private var contextMenu: ContextMenu? = null
	private var subArray: BindableJsonArray? = null

	init {
		if (!schema.readOnly) {
			node.onContextMenuRequested = EventHandler({ showContextMenu(null, it.screenX, it.screenY) })
		}
	}

	override fun bindTo(type: BindableJsonType) {
		bound = type
		subArray = createSubArray(type)
		updateChildCount()
		bound = type
	}

	private fun createSubArray(parent: BindableJsonType): BindableJsonArray {
		var arr = parent.getValue(schema) as? JSONArray
		if (arr == null) {
			arr = JSONArray()
			parent.setValue(schema, arr)
		}
		return BindableJsonArray(parent, arr)
	}

	private fun showContextMenu(clickedItem: TypeControl?, x: Double, y: Double) {
		contextMenu?.hide()
		val addEntry = MenuItem("+")
		val indexOfClicked = children.indexOf(clickedItem)
		addEntry.onAction = EventHandler({ addItem(indexOfClicked + 1) })
		val contextMenu = ContextMenu(addEntry)
		if (clickedItem != null) {
			val removeEntry = MenuItem("-")
			removeEntry.onAction = EventHandler({ removeItem(clickedItem) })
			contextMenu.items.add(removeEntry)
			if (indexOfClicked != 0) {
				val upEntry = MenuItem("↑")
				upEntry.onAction = EventHandler({ moveItemUp(clickedItem) })
				contextMenu.items.addAll(SeparatorMenuItem(), upEntry)
			}
			if (indexOfClicked != children.size - 1) {
				val downEntry = MenuItem("↓")
				downEntry.onAction = EventHandler({ moveItemDown(clickedItem) })
				if (indexOfClicked == 0) {
					contextMenu.items.addAll(SeparatorMenuItem(), downEntry)
				} else {
					contextMenu.items.add(downEntry)
				}
			}
		}
		this.contextMenu = contextMenu
		contextMenu.show(node, x, y)
	}

	private fun updateChildCount() {
		val subArray = subArray
		var children = bound?.getValue(schema) as? JSONArray
		if (children == null) {
			children = JSONArray()
		}
		while (this.children.size > children.length()) {
			content.getChildren().remove(this.children.removeAt(0).node)
		}
		while (this.children.size < children.length()) {
			val new =
				ControlFactory.convert(SchemaWrapper(schema, contentSchema, this.children.size.toString()), refProvider)
			this.children.add(new)
			this.content.getChildren().add(new.node)
			new.node.onContextMenuRequested = EventHandler({
				showContextMenu(new, it.screenX, it.screenY)
				it.consume()
			})
		}
		if (subArray != null) {
			for (i in 0 until children.length()) {
				val obj = BindableJsonArrayEntry(subArray, i)
				this.children.get(i).bindTo(obj)
			}
		}
		bound?.setValue(schema, children)
	}

	private fun addItem(pos: Int) {
		val children = bound?.getValue(schema) as? JSONArray
		if (children == null) return
		for (i in children.length() downTo pos + 1) {
			val tmp = children.get(i - 1)
			children.put(i, tmp)
		}
		children.put(pos, JSONObject.NULL)
		updateChildCount()
	}

	private fun removeItem(toRemove: TypeControl) {
		val children = bound?.getValue(schema) as? JSONArray
		if (children == null) return
		val index = this.children.indexOf(toRemove)
		children.remove(index)
		updateChildCount()
	}

	private fun moveItemUp(toMove: TypeControl) {
		val children = bound?.getValue(schema) as? JSONArray
		if (children == null) return
		val index = this.children.indexOf(toMove)
		if (index == 0) return
		val tmp = children.get(index - 1)
		children.put(index - 1, children.get(index))
		children.put(index, tmp)
		updateChildCount()
	}

	private fun moveItemDown(toMove: TypeControl) {
		val children = bound?.getValue(schema) as? JSONArray
		if (children == null) return
		val index = this.children.indexOf(toMove)
		if (index >= children.length()) return
		val tmp = children.get(index + 1)
		children.put(index + 1, children.get(index))
		children.put(index, tmp)
		updateChildCount()
	}


}