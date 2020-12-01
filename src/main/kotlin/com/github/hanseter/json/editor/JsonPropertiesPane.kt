package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.actions.EditorAction
import com.github.hanseter.json.editor.controls.ArrayControl
import com.github.hanseter.json.editor.controls.ObjectControl
import com.github.hanseter.json.editor.controls.TypeControl
import com.github.hanseter.json.editor.extensions.SimpleEffectiveSchema
import com.github.hanseter.json.editor.ui.ControlTreeItemData
import com.github.hanseter.json.editor.ui.FilterableTreeItem
import com.github.hanseter.json.editor.ui.StyledTreeItemData
import com.github.hanseter.json.editor.ui.TreeItemData
import com.github.hanseter.json.editor.util.EditorContext
import com.github.hanseter.json.editor.util.PropertyGrouping
import com.github.hanseter.json.editor.util.RootBindableType
import com.github.hanseter.json.editor.util.ViewOptions
import com.github.hanseter.json.editor.validators.Validator
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.Event
import org.everit.json.schema.Schema
import org.everit.json.schema.ValidationException
import org.json.JSONArray
import org.json.JSONObject

class JsonPropertiesPane(
        title: String,
        private val objId: String,
        data: JSONObject,
        schema: Schema,
        private val refProvider: IdReferenceProposalProvider,
        private val actions: List<EditorAction>,
        private val validators: List<Validator>,
        viewOptions: ViewOptions,
        private val changeListener: (JSONObject) -> JSONObject
) {
    val treeItem: FilterableTreeItem<TreeItemData> = FilterableTreeItem(StyledTreeItemData(title, listOf("isRootRow")))
    private val schema = SimpleEffectiveSchema(null, schema, title)
    private var objectControl: TypeControl? = null
    private var controlItem: FilterableTreeItem<TreeItemData>? = null
    private val contentHandler = ContentHandler(data)
    val valid = SimpleBooleanProperty(true)
    var viewOptions: ViewOptions = viewOptions
        set(value) {
            field = value
            createControlTree()
            controlItem?.also { item ->
                updateTreeUiElements(item, contentHandler.data)
            }
        }

    init {
        treeItem.isExpanded = false
        treeItem.expandedProperty().addListener { _, _, new ->
            if (new) {
                contentHandler.handleExpansion()
            }
        }
    }

    private fun initObjectControl() {
        if (objectControl != null) return
        this.objectControl = ControlFactory.convert(schema, EditorContext(refProvider, objId, ::updateTreeAfterChildChange))
        createControlTree()
    }

    private fun createControlTree() {
        val controlItem = objectControl?.let { wrapControlInTreeItem(it) } ?: return
        this.controlItem?.also {
            it.list.clear()
            Bindings.unbindContent(treeItem.list, it.list)
        }
        this.controlItem = controlItem
        if (controlItem.isLeaf) treeItem.add(controlItem)
        else Bindings.bindContent(treeItem.list, controlItem.list)
    }

    private fun wrapControlInTreeItem(control: TypeControl): FilterableTreeItem<TreeItemData> {
        val actionHandler: (Event, EditorAction, TypeControl) -> Unit = { e, action, source ->
            val ret = action.apply(contentHandler.data, source.model, e)
            if (ret != null) {
                val newData = changeListener(ret)
                fillData(newData)
            }
        }

        val item: FilterableTreeItem<TreeItemData> =
                FilterableTreeItem(ControlTreeItemData(control, actions, actionHandler, objId, validators.filter { it.selector.matches(control.model) }))
        if (control is ObjectControl) {
            addObjectControlChildren(item, control)
        } else {
            item.list.setAll(control.childControls.map { wrapControlInTreeItem(it) })
        }
        item.isExpanded = item.list.size <= viewOptions.collapseThreshold
        return item
    }

    fun fillData(data: JSONObject) {
        contentHandler.updateData(data)
    }

    private fun fillSheet(data: JSONObject) {
        val type = RootBindableType(data)
        objectControl?.bindTo(type)
        fillTree(data)
        type.registerListener {
            val newData = changeListener(type.value!!)
            fillData(newData)
        }
    }

    private fun fillTree(data: JSONObject) {
        controlItem?.also { item ->
            objectControl?.also {
                updateTree(item, it)
            }
            updateTreeUiElements(item, data)
        }
    }

    private fun updateTreeAfterChildChange(control: TypeControl) {
        val item = findInTree(treeItem, control) ?: return
        val iterNewChilds = control.childControls.listIterator()
        val iterOldChilds = item.list.listIterator()
        while (iterNewChilds.hasNext() && iterOldChilds.hasNext()) {
            val newChild = iterNewChilds.next()
            val oldChild = iterOldChilds.next()
            if (newChild::class != oldChild::class) {
                iterOldChilds.set(wrapControlInTreeItem(newChild))
            }
        }
        iterNewChilds.forEachRemaining {
            iterOldChilds.add(wrapControlInTreeItem(it))
        }
        while (iterOldChilds.hasNext()) {
            iterOldChilds.remove()
        }
    }

    private fun findInTree(item: FilterableTreeItem<TreeItemData>, control: TypeControl): FilterableTreeItem<TreeItemData>? =
            item.list.find { (it.value as? ControlTreeItemData)?.typeControl == control }
                    ?: item.list.asSequence().mapNotNull { findInTree(it, control) }.firstOrNull()

    private fun updateTree(item: FilterableTreeItem<TreeItemData>, control: TypeControl) {
        when (control) {
            is ObjectControl -> updateObjectControlInTree(item, control)
            is ArrayControl -> updateArrayControlInTree(item, control)
        }
        item.children.forEach { child ->
            (child.value as? ControlTreeItemData)?.also { updateTree(child as FilterableTreeItem<TreeItemData>, it.typeControl) }
        }
    }

    private fun updateObjectControlInTree(item: FilterableTreeItem<TreeItemData>, control: ObjectControl) {
        if (item.isLeaf) {
            if (control.childControls.isNotEmpty()) {
                addObjectControlChildren(item, control)
            }
        } else {
            if (control.childControls.isEmpty()) {
                item.clear()
            }
        }
    }

    private fun updateArrayControlInTree(item: FilterableTreeItem<TreeItemData>, control: TypeControl) {
        while (item.list.size > control.childControls.size) {
            item.list.removeLast()
        }
        item.addAll((item.list.size until control.childControls.size).map { wrapControlInTreeItem(control.childControls[it]) })
    }

    private fun addObjectControlChildren(node: FilterableTreeItem<TreeItemData>, control: ObjectControl) {
        val propOrder = control.model.schema.propertyOrder
        if (viewOptions.groupBy == PropertyGrouping.REQUIRED) {
            addRequiredAndOptionalChildren(node,
                    control.requiredChildren.sortedWith(PropOrderComparator(propOrder)),
                    control.optionalChildren.sortedWith(PropOrderComparator(propOrder)))
        } else {
            node.addAll(control.childControls
                    .sortedWith(PropOrderComparator(propOrder))
                    .map { wrapControlInTreeItem(it) })
        }
    }

    private class PropOrderComparator(private val wantedOrder: List<String>) : Comparator<TypeControl> {
        override fun compare(o1: TypeControl, o2: TypeControl): Int {
            val prop1 = o1.model.schema.propertyName
            val prop2 = o2.model.schema.propertyName

            val index1 = wantedOrder.indexOf(prop1)
            val index2 = wantedOrder.indexOf(prop2)

            val compareOrdered = index1.compareTo(index2)

            return if (compareOrdered != 0) compareOrdered
            else o1.model.schema.title.toLowerCase().compareTo(o2.model.schema.title.toLowerCase())
        }

    }

    private fun createRequiredHeader(): FilterableTreeItem<TreeItemData> = FilterableTreeItem(StyledTreeItemData("Required", listOf("isHeadlineRow")))

    private fun createOptionalHeader(): FilterableTreeItem<TreeItemData> = FilterableTreeItem(StyledTreeItemData("Optional", listOf("isHeadlineRow")))

    private fun addRequiredAndOptionalChildren(node: FilterableTreeItem<TreeItemData>, required: List<TypeControl>, optional: List<TypeControl>) {
        if (required.isNotEmpty()) {
            node.add(createRequiredHeader())
            node.addAll(required.map { wrapControlInTreeItem(it) })
        }

        if (optional.isNotEmpty()) {
            node.add(createOptionalHeader())
            node.addAll(optional.map { wrapControlInTreeItem(it) })
        }
    }

    private fun updateTreeUiElements(root: FilterableTreeItem<TreeItemData>, data: JSONObject) {
        val errorMap = mutableMapOf<JSONPointer, MutableList<String>>()
        val parentErrorCount = mutableMapOf<JSONPointer, Int>()
        fun addError(pointer: List<String>, message: String) {
            val pointers = pointer.heads()
            pointers.dropLast(1).forEach { parentPointer ->
                val count = parentErrorCount[parentPointer] ?: 0
                parentErrorCount[parentPointer] = count + 1
            }
            errorMap.getOrPut(pointer, { mutableListOf() }).add(message)
        }
        validateSchema(prepareForValidation(root, data), ::addError)
        flattenBottomUp(root).forEach { item ->
            (item.value as? ControlTreeItemData)?.also { data ->
                val pointer = listOf("#") + data.typeControl.model.schema.pointer
                data.validators.flatMap { it.validate(data.typeControl.model, objId) }.forEach { addError(pointer, it) }
                data.validationMessage = createErrorMessage(parentErrorCount[pointer]
                        ?: 0, errorMap[pointer])
            }
            item.value.updateFinished()
        }
        treeItem.value.validationMessage = createErrorMessage(parentErrorCount[listOf("#")]
                ?: 0, errorMap[listOf("#")])
        treeItem.value.updateFinished()
    }

    private fun createErrorMessage(subErrors: Int, errors: List<String>?): String? {
        val subErrorMessage = if (subErrors > 0) {
            "$subErrors sub-error" + if (subErrors > 1) "s" else ""
        } else null
        val errorMessage = errors?.joinToString("\n")
        return when {
            subErrorMessage != null && errorMessage != null -> subErrorMessage + "\n" + errorMessage
            errorMessage != null -> errorMessage
            subErrorMessage != null -> subErrorMessage
            else -> null
        }
    }

    private fun prepareForValidation(root: FilterableTreeItem<TreeItemData>, data: JSONObject): JSONObject {
        val copy = deepCopyForJson(data)
        flattenBottomUp(root).map { it.value }.filterIsInstance<ControlTreeItemData>().forEach {
            val schema = it.typeControl.model.schema
            val defaultValue = schema.defaultValue
            if (defaultValue != null) {
                val pointer = schema.pointer
                val parent = org.json.JSONPointer(pointer.dropLast(1)).queryFrom(copy) as? JSONObject
                if (parent != null && !parent.has(pointer.last())) {
                    parent.put(pointer.last(), schema.defaultValue)
                }
            }
        }
        return copy
    }

    private fun validateSchema(data: JSONObject, errorCollector: (JSONPointer, String) -> Unit) {
        try {
            schema.baseSchema.validate(data)
        } catch (e: ValidationException) {
            mapPointerToError(e, errorCollector)
        }
    }

    private fun <T> flattenBottomUp(item: FilterableTreeItem<T>): Sequence<FilterableTreeItem<T>> =
            item.list.asSequence().flatMap { flattenBottomUp(it) } + sequenceOf(item)

    private fun mapPointerToError(ex: ValidationException, errorCollector: (JSONPointer, String) -> Unit) {
        fun flatten(ex: ValidationException): Sequence<ValidationException> =
                if (ex.causingExceptions.isEmpty()) sequenceOf(ex)
                else ex.causingExceptions.asSequence().flatMap { flatten(it) }

        flatten(ex).forEach { validationError ->
            errorCollector(validationError.pointerToViolation.split('/'), validationError.errorMessage)
        }
    }

    private inner class ContentHandler(var data: JSONObject) {
        private var dataDirty = true

        fun handleExpansion() {
            if (objectControl == null) {
                initObjectControl()
            }
            if (dataDirty) {
                fillSheet(data)
                dataDirty = false
            }
        }

        fun updateData(data: JSONObject) {
            this.data = data
            if (treeItem.isExpanded) {
                fillSheet(data)
            } else {
                dataDirty = true
            }
        }
    }
}

fun <T> List<T>.heads(): List<List<T>> {
    tailrec fun <T> headsRec(list: List<T>, heads: List<List<T>>): List<List<T>> = when {
        list.isEmpty() -> heads
        else -> headsRec(list.dropLast(1), listOf(list) + heads)
    }
    return headsRec(this, emptyList())
}

private fun <T> deepCopyForJson(obj: T): T = when (obj) {
    is JSONObject -> obj.keySet().fold(JSONObject()) { acc, it ->
        acc.put(it, deepCopyForJson(obj.get(it)))
    } as T
    is JSONArray -> obj.fold(JSONArray()) { acc, it ->
        acc.put(deepCopyForJson(it))
    } as T
    else -> obj
}

typealias JSONPointer = List<String>
