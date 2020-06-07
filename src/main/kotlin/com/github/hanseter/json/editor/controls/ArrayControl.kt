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
import org.controlsfx.validation.ValidationSupport
import org.controlsfx.validation.Validator
import javafx.beans.property.SimpleBooleanProperty
import org.controlsfx.control.decoration.Decorator
import org.controlsfx.control.decoration.Decoration
import org.controlsfx.control.decoration.GraphicDecoration
import org.controlsfx.validation.decoration.GraphicValidationDecoration
import sun.security.validator.SimpleValidator
import org.controlsfx.validation.ValidationMessage
import org.controlsfx.validation.Severity
import javafx.scene.control.Control
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label

class ArrayControl(
	override val schema: SchemaWrapper<ArraySchema>,
	private val contentSchema: Schema,
	private val refProvider: IdReferenceProposalProvider
) :
	TypeWithChildrenControl(schema, listOf(), refProvider) {

	//	private val validation = ValidationSupport()
	private var bound: BindableJsonType? = null
	private var contextMenu: ContextMenu? = null
	private var subArray: BindableJsonArray? = null

	init {
		if (!schema.readOnly) {
			node.onContextMenuRequested = EventHandler({ showContextMenu(null, it.screenX, it.screenY) })
		}
//		addLengthValidation(schema.schema.getMinItems(), schema.schema.getMaxItems())
//		validation.redecorate()
	}

//	private fun addLengthValidation(minLength: Int?, maxLength: Int?) {
//		if (minLength != null) {
//			validation.registerValidator(this.node, createMinLengthValidator(minLength))
//		}
//		if (maxLength != null) {
//			validation.registerValidator(this.node, craeteMaxLengthValidator(maxLength))
//		}
//	}

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
			content.getChildren().remove(this.children.removeAt(this.children.size - 1).node)
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
		Decorator.removeAllDecorations(this.node)
		if (hasTooManyItems(children.length())) {
			GraphicValidationDecorationWithPosition(Pos.TOP_LEFT).applyValidationDecoration(
				SimpleValidationMessage(
					this.node,
					"Must have at most " + schema.schema.getMaxItems() + " items",
					Severity.ERROR
				)
			)
		} else if (hasTooFewItems(children.length())) {
			GraphicValidationDecorationWithPosition(Pos.TOP_LEFT).applyValidationDecoration(
				SimpleValidationMessage(
					this.node,
					"Must have at least " + schema.schema.getMinItems() + " items",
					Severity.ERROR
				)
			)
		}
	}

	class GraphicValidationDecorationWithPosition(private val pos: Pos) : GraphicValidationDecoration() {
		companion object {
			const val SHADOW_EFFECT = "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);"
		}

		private fun createDecorationNode(message: ValidationMessage): Node {
			val graphic = if (Severity.ERROR == message.getSeverity()) createErrorNode() else createWarningNode()
			graphic.setStyle(SHADOW_EFFECT);
			val label = Label("", graphic)
			label.setTooltip(createTooltip(message));
			label.setAlignment(Pos.CENTER);
			return label;
		}

		override fun createValidationDecorations(message: ValidationMessage): Collection<Decoration> =
			listOf(GraphicDecoration(createDecorationNode(message), pos))
	}

	class SimpleValidationMessage(
		private val target: Control,
		private val text: String,
		private val severity: Severity
	) : ValidationMessage {
		override fun getTarget(): Control = target
		override fun getText(): String = text
		override fun getSeverity(): Severity = severity
	}

	private fun hasTooManyItems(childCount: Int) =
		schema.schema.getMaxItems() != null && childCount > schema.schema.getMaxItems()

	private fun hasTooFewItems(childCount: Int) =
		schema.schema.getMinItems() != null && childCount < schema.schema.getMinItems()

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