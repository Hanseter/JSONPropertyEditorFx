package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.ControlFactory
import com.github.hanseter.json.editor.actions.ActionTargetSelector
import com.github.hanseter.json.editor.actions.ActionsContainer
import com.github.hanseter.json.editor.actions.EditorAction
import com.github.hanseter.json.editor.extensions.ArraySchemaWrapper
import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.types.ArrayModel
import com.github.hanseter.json.editor.types.TypeModel
import com.github.hanseter.json.editor.util.BindableJsonArray
import com.github.hanseter.json.editor.util.BindableJsonArrayEntry
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.Control
import org.controlsfx.validation.Severity
import org.controlsfx.validation.ValidationMessage
import org.everit.json.schema.ArraySchema
import org.json.JSONArray
import org.json.JSONObject

class ArrayControl(override val model: ArrayModel, private val context: EditorContext) : TypeControl {

    private val editorActionsContainer = context.createActionContainer(this).let {
        if (!model.schema.readOnly) {
            ActionsContainer(it,
                    // \uD83D\uDFA3 = 🞣
                    listOf(ArrayAction("\uD83D\uDFA3", "Inserts a new empty item at the end of the list") {
                        model.addItemAt(children.lastIndex + 1)
                    }))
        } else it
    }

    private val statusControl = TypeWithChildrenStatusControl("To Empty List") {
        model.value = JSONArray()
        valuesChanged()
    }

    override val node = FilterableTreeItem(TreeItemData(model.schema.title, model.schema.schema.description, statusControl, editorActionsContainer))

    private val children = mutableListOf<TypeControl>()
    private var subArray: BindableJsonArray? = null
    private val validInternal = SimpleBooleanProperty(true)
    override val valid = SimpleBooleanProperty(true)

    override fun bindTo(type: BindableJsonType) {
        model.bound = type
        subArray = createSubArray(type, model.schema)
        valuesChanged()
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
                val childSchema = ArraySchemaWrapper(model.schema, model.contentSchema, currentChildIndex)
                val arrayActions = if (model.schema.readOnly) emptyList()
                else listOf(ArrayAction("-", "Remove this item") { model.removeItem(currentChildIndex) },
                        ArrayAction("↑", "Move this item one row up") { model.moveItemUp(currentChildIndex) },
                        ArrayAction("↓", "Move this item one row down") { model.moveItemDown(currentChildIndex) })
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
        val rawValues = model.rawValue
        var values = rawValues as? JSONArray
        if (rawValues != JSONObject.NULL) {
            if (values == null) {
                values = JSONArray()
            }

            removeAdditionalUiCells(values)
            addNeededUiCells(values)
            rebindChildren(subArray, values)
            model.value = values
        } else {
            removeAdditionalUiCells(JSONArray())
        }

        validInternal.set(model.validationErrors.isEmpty())
        valid.bind(validInternal.and(createValidityBinding(this.children)))

        updateLabel()
    }

    private fun updateLabel() {
        if (model.rawValue == JSONObject.NULL) {
            statusControl.displayNull()
        } else {
            statusControl.displayNonNull("[${children.size} Element${if (children.size == 1) "" else "s"}]")
        }
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

    private inner class ArrayChildWrapper(wrapped: TypeControl, arrayActions: List<ArrayAction>) : TypeControl by wrapped {
        override val node: FilterableTreeItem<TreeItemData>

        init {
            val origNode = wrapped.node
            val origItemData = origNode.value
            val actions = ActionsContainer(origItemData.action
                    ?: context.createActionContainer(this), arrayActions)

            this.node = FilterableTreeItem(TreeItemData(origItemData.label, origItemData.control, actions, origItemData.isRoot, origItemData.isHeadline))
            Bindings.bindContent(node.list, origNode.list)
        }

    }

    private class ArrayAction(override val text: String, override val description: String, private val action: () -> Unit) : EditorAction {
        override val selector: ActionTargetSelector = ActionTargetSelector.Always()
        override fun apply(currentData: JSONObject, model: TypeModel<*>): JSONObject? {
            action()
            return null
        }
    }
}

fun createSubArray(parent: BindableJsonType, schema: SchemaWrapper<ArraySchema>): BindableJsonArray? {
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