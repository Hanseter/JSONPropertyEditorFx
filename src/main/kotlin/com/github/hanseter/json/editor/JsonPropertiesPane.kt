package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.actions.ActionsContainer
import com.github.hanseter.json.editor.actions.EditorAction
import com.github.hanseter.json.editor.controls.ObjectControl
import com.github.hanseter.json.editor.controls.TypeControl
import com.github.hanseter.json.editor.extensions.*
import com.github.hanseter.json.editor.util.EditorContext
import com.github.hanseter.json.editor.util.RootBindableType
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.Control
import org.controlsfx.validation.Severity
import org.controlsfx.validation.ValidationMessage
import org.controlsfx.validation.decoration.GraphicValidationDecoration
import org.everit.json.schema.Schema
import org.json.JSONObject

class JsonPropertiesPane(
        title: String,
        data: JSONObject,
        schema: Schema,
        private val refProvider: IdReferenceProposalProvider,
        private val resolutionScopeProvider: ResolutionScopeProvider,
        private val actions: List<EditorAction>,
        private val validators: List<com.github.hanseter.json.editor.validators.Validator>,
        private val changeListener: (JSONObject, JsonPropertiesPane) -> Unit
) {
    val treeItem: FilterableTreeItem<TreeItemData> = FilterableTreeItem(SectionRootTreeItemData(title))
    private val schema = RegularSchemaWrapper(null, schema, title)
    private var objectControl: TypeControl? = null
    private var controlItem: FilterableTreeItem<TreeItemData>? = null
    private val contentHandler = ContentHandler(data)
    val valid = SimpleBooleanProperty(true)

    init {
        treeItem.isExpanded = false
        treeItem.expandedProperty().addListener { _, _, new ->
            if (new) {
                contentHandler.handleExpansion()
            }
        }
    }

    private fun initObjectControl() {
        val objectControl = ControlFactory.convert(schema, EditorContext(refProvider))
        controlItem = wrapControlInTreeItem(objectControl)
        if (controlItem!!.isLeaf) treeItem.add(controlItem!!)
        else Bindings.bindContent(treeItem.list, controlItem!!.list)
        this.objectControl = objectControl
    }

    private fun wrapControlInTreeItem(control: TypeControl): FilterableTreeItem<TreeItemData> {
        val actions = ActionsContainer(control, actions) { action, control ->
            val ret = action.apply(contentHandler.data, control.model)
            if (ret != null) {
                changeListener(ret, this)
                fillData(ret)
            }
        }

        val item: FilterableTreeItem<TreeItemData> =
                FilterableTreeItem(ControlTreeItemData(control, actions, validators.filter { it.selector.matches(control.model) }))
        if (control is ObjectControl) {
            addRequiredAndOptionalChildren(item, control.requiredChildren, control.optionalChildren)
        } else {
            item.list.setAll(control.childControls.map { wrapControlInTreeItem(it) })
        }
        return item
    }

    fun fillData(data: JSONObject) {
        contentHandler.updateData(data)
    }

    private fun fillSheet(data: JSONObject) {
        val type = RootBindableType(data)
        objectControl?.bindTo(type)
        fillTree()
        type.registerListener {
            changeListener(type.value!!, this)
        }
    }

    private fun fillTree() {
        controlItem?.also { item ->
            objectControl?.also {
                updateTree(item, it)
            }
            updateValidity(item)
        }
    }

    private fun updateTree(item: FilterableTreeItem<TreeItemData>, control: TypeControl) {
        if (control is ObjectControl) {
            if (item.isLeaf) {
                if (control.childControls.isNotEmpty()) {
                    addRequiredAndOptionalChildren(item, control.requiredChildren, control.optionalChildren)
                }
            } else {
                if (control.childControls.isEmpty()) {
                    item.clear()
                }
            }
        } else {
            while (item.list.size > control.childControls.size) {
                item.list.removeLast()
            }
            item.addAll((item.list.size until control.childControls.size).map { wrapControlInTreeItem(control.childControls[it]) })
            if (item.list.size != control.childControls.size) {
                item.list.setAll(control.childControls.map { wrapControlInTreeItem(it) })
            }
        }
        item.children.forEach { item ->
            (item.value as? ControlTreeItemData)?.also { updateTree(item as FilterableTreeItem<TreeItemData>, it.typeControl) }
        }
    }

    private fun createRequiredHeader(): FilterableTreeItem<TreeItemData> = FilterableTreeItem(HeaderTreeItemData("Required"))

    private fun createOptionalHeader(): FilterableTreeItem<TreeItemData> = FilterableTreeItem(HeaderTreeItemData("Optional"))

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

    private fun updateValidity(item: FilterableTreeItem<TreeItemData>) {
        val decoration = GraphicValidationDecoration()
        var hadValidationErrors = false
        val stack = ArrayList<FilterableTreeItem<TreeItemData>>()
        stack.add(item)
        while (stack.isNotEmpty()) {
            val current = stack.removeLast()
            current.value.actions?.updateDisablement()
            stack.addAll(current.list)
            (current.value as? ControlTreeItemData)?.also {data ->
                decoration.removeDecorations(data.label)
                val validationErrors = data.validators.flatMap { it.validate(data.typeControl.model) }
                if (validationErrors.isNotEmpty()) {
                    hadValidationErrors = true
                    validationErrors.forEach {
                        decoration.applyValidationDecoration(SimpleValidationMessage(data.label, it, Severity.ERROR))
                    }
                }
            }
        }
        valid.set(!hadValidationErrors)
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