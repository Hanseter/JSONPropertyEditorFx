package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.ControlFactory
import com.github.hanseter.json.editor.actions.ActionTargetSelector
import com.github.hanseter.json.editor.actions.EditorAction
import com.github.hanseter.json.editor.extensions.ArraySchemaWrapper
import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.util.BindableJsonArray
import com.github.hanseter.json.editor.util.BindableJsonArrayEntry
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import javafx.beans.InvalidationListener
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Control
import org.controlsfx.validation.Severity
import org.controlsfx.validation.ValidationMessage
import org.everit.json.schema.ArraySchema
import org.everit.json.schema.Schema
import org.json.JSONArray
import org.json.JSONObject

class ArrayControl(override val schema: SchemaWrapper<ArraySchema>, private val contentSchema: Schema, private val context: EditorContext)
    : TypeControl {

    private val editorActionsContainer = context.createActionContainer(this,
            // \uD83D\uDFA3 = 🞣
            additionalActions = listOf(ArrayAction("\uD83D\uDFA3", "Inserts a new empty item at the end of the list") {
                addItemAt(children.lastIndex + 1)
            }))

    private val statusControl = TypeWithChildrenStatusControl("To Empty List") {
        bound?.setValue(schema, JSONArray())
        valuesChanged()
    }

    override val node = FilterableTreeItem(TreeItemData(schema.title, null, statusControl, editorActionsContainer))

    private var bound: BindableJsonType? = null
    private val children = mutableListOf<TypeControl>()
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
        itemCountValidationMessage.addListener(onValidationStateChanged)
        uniqueItemValidationMessage.addListener(onValidationStateChanged)
    }

    override fun bindTo(type: BindableJsonType) {
        bound = type
        subArray = createSubArray(type)
        valuesChanged()
    }

    private fun createSubArray(parent: BindableJsonType): BindableJsonArray? {
        val rawArr = parent.getValue(schema)

        if (rawArr == JSONObject.NULL) {
            return null
        }

        var arr = rawArr as? JSONArray
        if (arr == null) {
            arr = JSONArray()
            parent.setValue(schema, arr)
        }
        return BindableJsonArray(parent, arr)
    }

    private fun valuesChanged() {
        fun rebindChildren(subArray: BindableJsonArray?, values: JSONArray) {
            if (subArray != null) {
                for (i in 0 until values.length()) {
                    val obj = BindableJsonArrayEntry(subArray, i)
                    children[i].bindTo(obj)
                }
            }
        }

        fun addNeededUiCells(values: JSONArray) {
            while (children.size < values.length()) {
                val currentChildIndex = children.size
                val childSchema = ArraySchemaWrapper(schema, contentSchema, currentChildIndex)
                val arrayActions = if (schema.readOnly) emptyList()
                else listOf(ArrayAction("-", "Remove this item") { removeItem(currentChildIndex) },
                        ArrayAction("↑", "Move this item one row up") { moveItemUp(currentChildIndex) },
                        ArrayAction("↓", "Move this item one row down") { moveItemDown(currentChildIndex) })
                val control = ControlFactory.convert(childSchema, context)

                val new = ArrayChildWrapper(control, arrayActions)
                children.add(new)
                node.add(new.node)
            }
        }

        fun removeAdditionalUiCells(values: JSONArray) {
            while (children.size > values.length()) {
                node.remove(children.removeAt(children.size - 1).node)
            }
        }

        val subArray = subArray
        val rawValues = bound?.getValue(schema)
        var values = rawValues as? JSONArray
        if (rawValues != JSONObject.NULL) {
            if (values == null) {
                values = JSONArray()
            }

            removeAdditionalUiCells(values)
            addNeededUiCells(values)
            rebindChildren(subArray, values)
            bound?.setValue(schema, values)
            validateChildCount(values)
        } else {
            removeAdditionalUiCells(JSONArray())
        }

        validateChildUniqueness()
        valid.bind(validInternal.and(createValidityBinding(this.children)))

        updateLabel()
    }

    private fun updateLabel() {
        if (bound?.getValue(schema) == JSONObject.NULL) {
            statusControl.displayNull()
        } else {
            statusControl.displayNonNull("[${children.size} Element${if (children.size == 1) "" else "s"}]")
        }
    }

    private fun validateChildCount(children: JSONArray) {
        itemCountValidationMessage.set(
                when {
                    hasTooManyItems(children.length()) -> SimpleValidationMessage(
                            this.node.value.control as Control,
                            "Must have at most " + schema.schema.maxItems + " items",
                            Severity.ERROR
                    )
                    hasTooFewItems(children.length()) -> SimpleValidationMessage(
                            this.node.value.control as Control,
                            "Must have at least " + schema.schema.minItems + " items",
                            Severity.ERROR
                    )
                    else -> null
                }
        )
    }

    private fun hasTooManyItems(childCount: Int) =
            schema.schema.maxItems != null && childCount > schema.schema.maxItems

    private fun hasTooFewItems(childCount: Int) =
            schema.schema.minItems != null && childCount < schema.schema.minItems

    class SimpleValidationMessage(
            private val target: Control,
            private val text: String,
            private val severity: Severity
    ) : ValidationMessage {
        override fun getTarget(): Control = target
        override fun getText(): String = text
        override fun getSeverity(): Severity = severity
    }

    private fun addItemAt(position: Int) {
        var children = bound?.getValue(schema) as? JSONArray

        if (children == null) {
            children = JSONArray()
            bound?.setValue(schema, children)
        }

        children.put(position, JSONObject.NULL)
        valuesChanged()
    }

    private fun removeItem(index: Int) {
        val children = bound?.getValue(schema) as? JSONArray ?: return
        children.remove(index)
        valuesChanged()
    }

    private fun moveItemUp(index: Int) {
        val children = bound?.getValue(schema) as? JSONArray ?: return
        if (index == 0) return
        val tmp = children.get(index - 1)
        children.put(index - 1, children.get(index))
        children.put(index, tmp)
        valuesChanged()
    }

    private fun moveItemDown(index: Int) {
        val children = bound?.getValue(schema) as? JSONArray ?: return
        if (index >= children.length() - 1) return
        val tmp = children.get(index + 1)
        children.put(index + 1, children.get(index))
        children.put(index, tmp)
        valuesChanged()
    }

    private fun validateChildUniqueness() {
        if (!schema.schema.needsUniqueItems()) return
        val children = bound?.getValue(schema) as? JSONArray ?: return

        for (i in 0 until children.length()) {
            for (j in i + 1 until children.length()) {
                if (areSame(children.get(i), children.get(j))) {
                    uniqueItemValidationMessage.set(
                            SimpleValidationMessage(
                                    this.node.value.control as Control,
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

    private fun areSame(a: Any?, b: Any?) = when (a) {
        is JSONObject -> a.similar(b)
        is JSONArray -> a.similar(b)
        else -> a == b
    }

    private inner class ArrayChildWrapper(wrapped: TypeControl, arrayActions: List<ArrayAction>) : TypeControl by wrapped {
        override val node: FilterableTreeItem<TreeItemData>

        init {
            val origNode = wrapped.node
            val children = origNode.list.toList()
            origNode.clear()
            val origItemData = origNode.value
            val actions = context.createActionContainer(this, additionalActions = arrayActions)

            this.node = FilterableTreeItem(TreeItemData(origItemData.key, origItemData.description, origItemData.control, actions, origItemData.isRoot, origItemData.isHeadline))
            node.addAll(children)
        }

    }

    private fun redecorate() {
        redecorate(this.node.value.control, itemCountValidationMessage, uniqueItemValidationMessage)
    }

    private class ArrayAction(override val text: String, override val description: String, private val action: () -> Unit) : EditorAction {
        override val selector: ActionTargetSelector = ActionTargetSelector.Always()
        override fun apply(currentValue: JSONObject, schema: SchemaWrapper<*>): JSONObject? {
            action()
            return null
        }
    }
}