package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.ControlFactory
import com.github.hanseter.json.editor.IdReferenceProposalProvider
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonArray
import com.github.hanseter.json.editor.util.BindableJsonArrayEntry
import com.github.hanseter.json.editor.util.BindableJsonType
import javafx.beans.InvalidationListener
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.VBox
import org.controlsfx.control.decoration.Decoration
import org.controlsfx.control.decoration.Decorator
import org.controlsfx.control.decoration.GraphicDecoration
import org.controlsfx.validation.Severity
import org.controlsfx.validation.ValidationMessage
import org.controlsfx.validation.decoration.GraphicValidationDecoration
import org.everit.json.schema.ArraySchema
import org.everit.json.schema.Schema
import org.json.JSONArray
import org.json.JSONObject

class ArrayControl(
	override val schema: SchemaWrapper<ArraySchema>,
	private val contentSchema: Schema,
	private val refProvider: IdReferenceProposalProvider
) : TypeWithChildrenControl(schema) {

	private val content = VBox()
	override val children =  mutableListOf<TypeControl>()

	private var bound: BindableJsonType? = null
	private var contextMenu: ContextMenu? = null
	private var subArray: BindableJsonArray? = null
	private val itemCountValidationMessage = SimpleObjectProperty<ValidationMessage?>(null)
	private val uniqueItemValidationMessage = SimpleObjectProperty<ValidationMessage?>(null)
	private val validInternal = SimpleBooleanProperty(true)
	override val valid = SimpleBooleanProperty(true)
	private val onValidationStateChanged = InvalidationListener {
		redecorate()
		validInternal.set(itemCountValidationMessage.get() == null && uniqueItemValidationMessage.get() == null)
	}

	init {
		if (!schema.readOnly) {
			node.onContextMenuRequested = EventHandler { showContextMenu(null, it.screenX, it.screenY) }
		}
		node.content = content
		itemCountValidationMessage.addListener(onValidationStateChanged)
		uniqueItemValidationMessage.addListener(onValidationStateChanged)
	}

	override fun bindTo(type: BindableJsonType) {
		bound = type
		subArray = createSubArray(type)
		updateChildCount()
		validateChildUniqueness()
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
		validateChildCount(children)
		valid.bind(validInternal.and(createValidityBinding()))

	}

	private fun redecorate() {
		Decorator.removeAllDecorations(this.node)
		val message = itemCountValidationMessage.get() ?: uniqueItemValidationMessage.get()
		if (message != null) {
			GraphicValidationDecorationWithPosition(Pos.TOP_LEFT).applyValidationDecoration(message)
		}
	}

	private fun validateChildCount(children: JSONArray) {
		itemCountValidationMessage.set(
			when {
				hasTooManyItems(children.length()) -> SimpleValidationMessage(
					this.node,
					"Must have at most " + schema.schema.getMaxItems() + " items",
					Severity.ERROR
				)
				hasTooFewItems(children.length()) -> SimpleValidationMessage(
					this.node,
					"Must have at least " + schema.schema.getMinItems() + " items",
					Severity.ERROR
				)
				else -> null
			}
		)
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
		validateChildUniqueness()
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

	private fun validateChildUniqueness() {
		if (!schema.schema.needsUniqueItems()) return
		val children = bound?.getValue(schema) as? JSONArray
		if (children == null) return
		for (i in 0 until children.length()) {
			for (j in i + 1 until children.length()) {
				if (areSame(children.get(i), children.get(j))) {
					uniqueItemValidationMessage.set(
						SimpleValidationMessage(
							this.node,
							"Items $i and $j are identical",
							Severity.ERROR
						)
					)
					return
				}
			}
		}
		uniqueItemValidationMessage.set(null)
	}

	private fun areSame(a: Any?, b: Any?) = when {
		a is JSONObject -> a.similar(b)
		a is JSONArray -> a.similar(b)
		else -> a == b
	}
}