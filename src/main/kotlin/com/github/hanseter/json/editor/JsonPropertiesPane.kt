package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.actions.EditorAction
import com.github.hanseter.json.editor.controls.ArrayControl
import com.github.hanseter.json.editor.controls.ObjectControl
import com.github.hanseter.json.editor.controls.TupleControl
import com.github.hanseter.json.editor.controls.TypeControl
import com.github.hanseter.json.editor.extensions.SimpleEffectiveSchema
import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import com.github.hanseter.json.editor.ui.*
import com.github.hanseter.json.editor.util.*
import com.github.hanseter.json.editor.validators.JSONPointer
import com.github.hanseter.json.editor.validators.ValidationEngine
import com.github.hanseter.json.editor.validators.Validator
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.Event
import org.controlsfx.validation.Severity
import org.json.JSONObject
import java.net.URI
import java.util.function.Supplier

class JsonPropertiesPane(
    private val title: String,
    private val objId: String,
    data: JSONObject,
    private var parsedSchema: ParsedSchema,
    private val readOnly: Boolean,
    private val resolutionScope: URI?,
    private val refProvider: Supplier<IdReferenceProposalProvider>,
    private val actions: List<EditorAction>,
    private val validators: () -> List<Validator>,
    viewOptions: ViewOptions,
    private val customizationObject: () -> CustomizationObject,
    private val changeListener: JsonPropertiesEditor.OnEditCallback
) {
    val treeItem: FilterableTreeItem<TreeItemData> =
        FilterableTreeItem(StyledTreeItemData(title, listOf(ROOT_ROW_CSS_CLASS)))
    private var schema = SimpleEffectiveSchema(null, parsedSchema.parsed, title)
    private val contentHandler = ContentHandler(data)
    val valid = SimpleBooleanProperty(true)
    var viewOptions: ViewOptions = viewOptions
        set(value) {
            field = value
            controlItem = wrapControlInTreeItem(objectControl)
            updateTreeUiElements(controlItem, contentHandler.data)
        }

    private var objectControl: TypeControl = initObjectControl()
        set(value) {
            field = value
            controlItem = wrapControlInTreeItem(field)
        }
    private var controlItem: FilterableTreeItem<TreeItemData> =
        wrapControlInTreeItem(objectControl).also {
            if (it.isLeaf) treeItem.add(it)
            else Bindings.bindContent(treeItem.list, it.list)
        }
        set(value) {
            field.list.clear()
            Bindings.unbindContent(treeItem.list, field.list)
            field = value
            treeItem.clear()
            if (field.isLeaf) treeItem.add(field)
            else Bindings.bindContent(treeItem.list, field.list)
        }

    init {
        treeItem.isExpanded = false
        treeItem.expandedProperty().addListener { _, _, new ->
            if (new) contentHandler.handleExpansion()
        }
    }

    private fun initObjectControl(): TypeControl {
        return ControlFactory.convert(
            schema,
            EditorContext(
                refProvider,
                objId,
                ::updateTreeAfterChildChange,
                viewOptions.idRefDisplayMode,
                viewOptions.decimalFormatSymbols,
            )
        )
    }

    fun rebuildControlTree() {
        val uiState = saveUiState()

        objectControl = initObjectControl()

        loadUiState(uiState)
    }

    private fun wrapControlInTreeItem(control: TypeControl): FilterableTreeItem<TreeItemData> {
        val item: FilterableTreeItem<TreeItemData> =
            FilterableTreeItem(
                ControlTreeItemData(
                    control,
                    actions,
                    createActionHandler(),
                    objId,
                    customizationObject
                )
            )
        if (control is ObjectControl) {
            addObjectControlChildren(item, control)
        } else {
            item.list.setAll(control.childControls.map { wrapControlInTreeItem(it) })
        }
        item.isExpanded = item.list.size <= viewOptions.collapseThreshold
        return item
    }

    private fun createActionHandler(): (Event, EditorAction, TypeControl) -> Unit {
        return lambda@{ e, action, source ->
            val ret =
                action.apply(
                    PropertiesEditInput(contentHandler.data, parsedSchema.raw),
                    source.model,
                    e
                )
            if (ret == null) return@lambda

            val actionSchema = ret.schema

            val newData = changeListener(
                PropertiesEditInput(ret.data, actionSchema ?: parsedSchema.raw)
            )

            val newSchema = newData.schema ?: actionSchema

            if (newSchema != null) {
                val newParsedSchema = ParsedSchema.create(newSchema, resolutionScope, readOnly)
                if (newParsedSchema != null) updateSchema(newParsedSchema)
            }

            fillData(newData.data)
        }
    }

    fun fillData(data: JSONObject) {
        contentHandler.updateData(data)
    }

    fun updateSchemaIfChanged(new: JSONObject) {
        if (new.similar(parsedSchema.raw)) return
        val parsedSchema = ParsedSchema.create(new, resolutionScope, readOnly) ?: return
        updateSchemaAndFillData(parsedSchema)
    }

    fun updateSchemaIfChanged(new: ParsedSchema) {
        if (new.raw.similar(parsedSchema.raw)) return
        updateSchemaAndFillData(new)
    }

    private fun updateSchemaAndFillData(schema: ParsedSchema) {
        val data = contentHandler.data
        updateSchema(schema)
        fillData(data)
    }

    private fun updateSchema(new: ParsedSchema) {
        parsedSchema = new
        schema = SimpleEffectiveSchema(null, parsedSchema.parsed, title)

        rebuildControlTree()
    }

    private fun fillSheet(data: JSONObject) {
        val type = RootBindableType(data)
        objectControl.bindTo(type)
        fillTree(data)
        type.registerListener {

            Platform.runLater {
                val newData = changeListener(PropertiesEditInput(type.getValue(), parsedSchema.raw))

                newData.schema
                    ?.let { ParsedSchema.create(it, resolutionScope, readOnly) }
                    ?.also { updateSchema(it) }

                fillData(newData.data)
            }
        }
    }

    private fun fillTree(data: JSONObject) {
        updateTree(controlItem, objectControl)
        updateTreeUiElements(controlItem, data)
    }

    private fun updateTreeAfterChildChange(control: TypeControl) {
        val item = findInTree(treeItem, control) ?: return

        item.list.setAll(control.childControls.map { wrapControlInTreeItem(it) })
    }

    private fun findInTree(
        item: FilterableTreeItem<TreeItemData>,
        control: TypeControl
    ): FilterableTreeItem<TreeItemData>? =
        item.list.find { (it.value as? ControlTreeItemData)?.typeControl == control }
            ?: item.list.asSequence().mapNotNull { findInTree(it, control) }.firstOrNull()

    private fun updateTree(item: FilterableTreeItem<TreeItemData>, control: TypeControl) {
        when (control) {
            is ObjectControl -> updateObjectControlInTree(item, control)
            is ArrayControl -> updateArrayControlInTree(item, control)
            is TupleControl -> updateTupleControlInTree(item, control)
        }
        item.children.forEach { child ->
            (child.value as? ControlTreeItemData)?.also {
                updateTree(
                    child as FilterableTreeItem<TreeItemData>,
                    it.typeControl
                )
            }
        }
    }

    private fun updateObjectControlInTree(
        item: FilterableTreeItem<TreeItemData>,
        control: ObjectControl
    ) {
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

    private fun updateArrayControlInTree(
        item: FilterableTreeItem<TreeItemData>,
        control: TypeControl
    ) {
        while (item.list.size > control.childControls.size) {
            item.list.removeLast()
        }
        item.addAll((item.list.size until control.childControls.size).map {
            wrapControlInTreeItem(
                control.childControls[it]
            )
        })
    }

    private fun updateTupleControlInTree(
        item: FilterableTreeItem<TreeItemData>,
        control: TupleControl
    ) {
        if (item.isLeaf) {
            if (control.childControls.isNotEmpty()) {
                item.addAll(control.childControls.map { wrapControlInTreeItem(it) })
            }
        } else {
            if (control.childControls.isEmpty()) {
                item.clear()
            }
        }
    }

    private fun addObjectControlChildren(
        node: FilterableTreeItem<TreeItemData>,
        control: ObjectControl
    ) {
        val propOrder = control.model.schema.propertyOrder
        if (viewOptions.groupBy == PropertyGrouping.REQUIRED) {
            addRequiredAndOptionalChildren(
                node,
                control.requiredChildren.sortedWith(PropOrderComparator(propOrder)),
                control.optionalChildren.sortedWith(PropOrderComparator(propOrder))
            )
        } else {
            node.addAll(
                control.childControls
                    .sortedWith(PropOrderComparator(propOrder))
                    .map { wrapControlInTreeItem(it) })
        }
    }

    private class PropOrderComparator(private val wantedOrder: List<String>) :
        Comparator<TypeControl> {
        override fun compare(o1: TypeControl, o2: TypeControl): Int {
            val prop1 = o1.model.schema.propertyName
            val prop2 = o2.model.schema.propertyName

            val index1 = wantedOrder.indexOf(prop1)
            val index2 = wantedOrder.indexOf(prop2)

            val compareOrdered = index1.compareTo(index2)

            return if (compareOrdered != 0) compareOrdered
            else o1.model.schema.title.lowercase().compareTo(o2.model.schema.title.lowercase())
        }

    }

    private fun createRequiredHeader(): FilterableTreeItem<TreeItemData> =
        FilterableTreeItem(StyledTreeItemData("Required", listOf("isHeadlineRow")))

    private fun createOptionalHeader(): FilterableTreeItem<TreeItemData> =
        FilterableTreeItem(StyledTreeItemData("Optional", listOf("isHeadlineRow")))

    private fun addRequiredAndOptionalChildren(
        node: FilterableTreeItem<TreeItemData>,
        required: List<TypeControl>,
        optional: List<TypeControl>
    ) {
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
        (root.value as? ControlTreeItemData)?.typeControl?.also { control ->
            val errors = ValidationEngine.validateData(control, objId, data, validators()).toMap()
            flattenBottomUp(root).forEach { item ->
                (item.value as? ControlTreeItemData)?.also { data ->
                    item.value.validationMessage =
                        generateErrorMessage(data.typeControl.model.schema.pointer, errors)
                }
                item.value.updateFinished()
            }
            treeItem.value.validationMessage = generateErrorMessage(listOf(), errors)
            treeItem.value.updateFinished()
            valid.set(errors.none { err -> err.value.any { it.severity == Severity.ERROR } })
        }
    }

    fun revalidate() {
        updateTreeUiElements(controlItem, contentHandler.data)
    }

    private fun saveUiState(): UiState = UiState(RowUiState(controlItem))

    private fun loadUiState(state: UiState) {
        state.rowState.apply(controlItem)
    }

    private fun generateErrorMessage(
        pointer: JSONPointer,
        errorMap: Map<JSONPointer, List<Validator.ValidationResult>>
    ): Validator.ValidationResult? {
        val error = errorMap[listOf("#") + pointer]

        // check for "missing key" error in parent
        if (pointer.isNotEmpty()) {
            val thisKey = pointer.last()

            errorMap[listOf("#") + pointer.dropLast(1)]?.let { parentError ->
                // Since we just get the errors as a string, comparing the message is pretty much
                // the best we can do. The only way it could be made more robust if we still had the
                // original validation error exception would be comparing
                // ValidationException#getKeyword to "required", which would be better, but not by much.
                if ("required key [$thisKey] not found" in parentError.map { it.message }) {
                    return Validator.SimpleValidationResult(
                        Severity.ERROR,
                        (error?.let { err ->
                            err.joinToString("\n") { it.message } + "\n${
                                JsonPropertiesMl.bundle.getString(
                                    "jsonEditor.validators.message.keyIsRequired"
                                )
                            }"
                        }
                            ?: JsonPropertiesMl.bundle.getString("jsonEditor.validators.message.keyIsRequired"))
                    )
                }
            }
        }

        return error?.let { err ->
            Validator.SimpleValidationResult(
                err.maxOf { it.severity },
                err.joinToString("\n") { it.message }
            )
        }
    }


    private fun <T> flattenBottomUp(item: FilterableTreeItem<T>): Sequence<FilterableTreeItem<T>> =
        item.list.asSequence().flatMap { flattenBottomUp(it) } + sequenceOf(item)

    fun expand(pointers: Set<List<String>>) {
        updateExpandedState(pointers, true)
    }

    fun expandAll() {
        flattenBottomUp(treeItem).forEach { it.isExpanded = true }
    }

    fun collapse(pointers: Set<List<String>>) {
        updateExpandedState(pointers, false)
    }

    fun collapseAll() {
        flattenBottomUp(treeItem).forEach { it.isExpanded = false }
        treeItem.isExpanded = true
    }

    private fun updateExpandedState(pointers: Set<List<String>>, expand: Boolean) {
        if (pointers.isEmpty()) return
        if (pointers.contains(emptyList())) {
            treeItem.isExpanded = expand
        }
        flattenBottomUp(treeItem).forEach {
            val value = it.value
            if (value is ControlTreeItemData && value.typeControl.model.schema.pointer in pointers) {
                it.isExpanded = expand
            }
        }
    }

    private inner class ContentHandler(var data: JSONObject) {
        private var dataDirty = true

        fun handleExpansion() {
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

    companion object {
        const val ROOT_ROW_CSS_CLASS = "isRootRow"
    }
}