package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.actions.*
import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import com.github.hanseter.json.editor.types.TypeModel
import com.github.hanseter.json.editor.ui.*
import com.github.hanseter.json.editor.util.*
import com.github.hanseter.json.editor.validators.IdReferenceValidator
import com.github.hanseter.json.editor.validators.Validator
import javafx.application.Platform
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ChangeListener
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.util.Callback
import org.controlsfx.validation.Severity
import org.controlsfx.validation.ValidationMessage
import org.json.JSONObject
import java.net.URI

class JsonPropertiesEditor @JvmOverloads constructor(
    private val readOnly: Boolean = false,
    viewOptions: ViewOptions = ViewOptions(),
    actions: List<EditorAction> = listOf(ResetToDefaultAction, ResetToNullAction),
    customizationObject: CustomizationObject = DefaultCustomizationObject,
    additionalValidators: List<Validator> = emptyList(),
    private val controlFactory: PropertyControlFactory = ControlFactory
) : StackPane() {
    var referenceProposalProvider: IdReferenceProposalProvider =
        IdReferenceProposalProvider.IdReferenceProposalProviderEmpty
    var resolutionScopeProvider: ResolutionScopeProvider =
        ResolutionScopeProvider.ResolutionScopeProviderEmpty

    private val fixedValidators = listOf(IdReferenceValidator { referenceProposalProvider })

    var additionalValidators: List<Validator> = additionalValidators
        set(value) {
            field = value.toList()
            validators = fixedValidators + field
        }

    // since this is read a lot more frequently than it's updated, we cache the complete validator
    // list instead of having it be computed every time
    var validators: List<Validator> = fixedValidators + additionalValidators
        private set(value) {
            field = value
            Platform.runLater {
                idsToPanes.values.forEach { it.revalidate() }
            }
        }

    var customizationObject: CustomizationObject = customizationObject
        set(value) {
            field = value

            Platform.runLater {
                idsToPanes.values.forEach { it.rebuildControlTree() }
            }
        }

    private val actions =
        actions + PreviewAction(
            viewOptions,
            { referenceProposalProvider },
            { resolutionScopeProvider }
        ) + arrayActions

    private val idsToPanes = mutableMapOf<String, JsonPropertiesPane>()
    private val rootItem: FilterableTreeItem<TreeItemData> =
        FilterableTreeItem(StyledTreeItemData("root", listOf()))

    private val keyColumn = createKeyColumn()

    private val treeTableView = TreeTableView<TreeItemData>().apply {
        id = "itemTable"
        rowFactory = Callback { TreeItemDataRow() }
        styleClass.add("json-properties-table")
        stylesheets.add(
            this@JsonPropertiesEditor.javaClass
                .getResource("TreeTableView.css")!!
                .toExternalForm()
        )
        columns.addAll(keyColumn, createControlColumn(), createActionColumn())
        isShowRoot = false
        columnResizePolicy = TreeTableView.CONSTRAINED_RESIZE_POLICY_FLEX_NEXT_COLUMN
        root = rootItem
        this.selectionModel.isCellSelectionEnabled = true
        TreeTableNavigation.addNavigationToTreeTableView(this)
    }

    private val scrollPane = ScrollPane().apply {
        id = "contentArea"
        content = treeTableView
        isFitToHeight = true
        isFitToWidth = true
    }

    private val _valid = SimpleBooleanProperty(true)
    private val paneValidListener =
        ChangeListener<Boolean> { _, _, _ -> _valid.set(idsToPanes.isEmpty() || idsToPanes.values.all { it.valid.get() }) }
    val valid: ReadOnlyBooleanProperty
        get() = _valid
    var viewOptions: ViewOptions = viewOptions
        set(value) {
            field = value
            idsToPanes.values.forEach { it.viewOptions = value }
        }

    private val filters = mutableListOf<ItemFilter>()

    private val _selectionModel =
        JsonPropertiesEditorSelectionModel.JsonPropertiesEditorSelectionModelImpl(
            this,
            idsToPanes,
            treeTableView
        )
    val selectionModel: JsonPropertiesEditorSelectionModel
        get() = _selectionModel

    init {
        this.children.addAll(scrollPane)
    }

    fun display(
        objId: String,
        title: String,
        obj: JSONObject,
        schema: ParsedSchema,
        callback: OnEditCallback
    ) {
        if (idsToPanes.contains(objId)) {
            updateObject(objId, obj, schema)
            return
        }

        val resolutionScope = resolutionScopeProvider.getResolutionScopeForElement(objId)

        val pane = createTitledPaneForSchema(
            title,
            objId,
            obj,
            schema,
            readOnly,
            resolutionScope,
            callback
        )
        pane.fillData(obj)
        idsToPanes[objId] = pane
        (treeTableView.root as FilterableTreeItem).add(pane.treeItem)
        pane.treeItem.isExpanded = idsToPanes.size <= viewOptions.numberOfInitiallyOpenedObjects
        pane.valid.addListener(paneValidListener)
        paneValidListener.changed(pane.valid, pane.valid.get(), pane.valid.get())
    }

    fun display(
        objId: String,
        title: String,
        obj: JSONObject,
        schema: JSONObject,
        callback: OnEditCallback
    ) {
        val parsedSchema =
            ParsedSchema.create(schema, resolutionScopeProvider.getResolutionScopeForElement(objId))
                ?: return
        display(objId, title, obj, parsedSchema, callback)
    }

    fun display(
        objId: String,
        title: String,
        obj: JSONObject,
        schema: JSONObject,
        callback: (JSONObject) -> JSONObject
    ) {
        display(objId, title, obj, schema) { it: PropertiesEditInput ->
            PropertiesEditResult(callback(it.data))
        }
    }

    /**
     * Displays a new element with. The schema needs to have been normalized via [SchemaNormalizer].
     */
    fun display(
        objId: String,
        title: String,
        obj: JSONObject,
        schema: ParsedSchema,
        callback: (JSONObject) -> JSONObject
    ) {
        display(objId, title, obj, schema) { it: PropertiesEditInput ->
            PropertiesEditResult(callback(it.data))
        }
    }

    /**
     * Updates the object without updating the schema
     */
    fun updateObject(
        objId: String,
        obj: JSONObject,
    ) {
        val pane = idsToPanes[objId] ?: return
        pane.fillData(obj)
    }

    /**
     * Updates the object, potentially updating the schema
     */
    fun updateObject(
        objId: String,
        obj: JSONObject,
        schema: JSONObject
    ) {
        val pane = idsToPanes[objId] ?: return
        pane.fillData(obj)
        pane.updateSchemaIfChanged(schema)
    }

    fun updateObject(
        objId: String,
        obj: JSONObject,
        schema: ParsedSchema
    ) {
        val pane = idsToPanes[objId] ?: return
        pane.fillData(obj)
        pane.updateSchemaIfChanged(schema)
    }

    fun removeObject(objId: String) {
        treeTableView.selectionModel.clearSelection()
        (idsToPanes.remove(objId))?.also {
            it.valid.removeListener(paneValidListener)
            (treeTableView.root as FilterableTreeItem).remove(it.treeItem)
        }
    }

    /**
     * Scrolls the editor to the element identified by [id].
     */
    fun scrollTo(id: String) {
        val (index, _) = _selectionModel.findIndexAndItemOfPane(id) ?: return
        if (index != -1) {
            treeTableView.scrollTo(index)
        }
    }

    /**
     * Scrolls the editor to matching [ElementField].
     * If the field is currently collapsed this will scroll to the nearest not collapsed element.
     * If the field does not exist, no scrolling will happen.
     */
    fun scrollToField(target: ElementField) {
        val index = selectionModel.findIndexOfVisibleParent(target)
        if (index != -1) {
            treeTableView.scrollTo(index)
        }
    }

    /**
     * Expands the part of the element identified by [id].
     * Specifically the subtrees identified by the [pointers].
     * An empty pointer will expand the root element.
     */
    fun expand(id: String, pointers: Set<List<String>>) {
        idsToPanes[id]?.expand(pointers)
    }

    /**
     * Expands all TreeItems of the element identified by [id].
     */
    fun expandAll(id: String) {
        idsToPanes[id]?.expandAll()
    }

    /**
     * Collapses the part of the element identified by [id].
     * Specifically the subtree identified by the [pointers].
     * An empty pointer will collapse the root element.
     */
    fun collapse(id: String, pointers: Set<List<String>>) {
        idsToPanes[id]?.collapse(pointers)
    }

    /**
     * Collapses all TreeItems of the element identified by [id].
     */
    fun collapseAll(id: String) {
        idsToPanes[id]?.collapseAll()
    }

    fun clear() {
        treeTableView.selectionModel.clearSelection()
        idsToPanes.clear()
        (treeTableView.root as FilterableTreeItem).clear()
        _valid.set(true)
    }

    private fun createTitledPaneForSchema(
        title: String,
        objId: String,
        data: JSONObject,
        schema: ParsedSchema,
        readOnly: Boolean,
        resolutionScope: URI?,
        callback: OnEditCallback
    ): JsonPropertiesPane = JsonPropertiesPane(
        title,
        objId,
        data,
        schema,
        readOnly,
        resolutionScope,
        { referenceProposalProvider },
        actions,
        { validators },
        viewOptions,
        controlFactory,
        { customizationObject },
        callback
    )

    private fun createKeyColumn(): TreeTableColumn<TreeItemData, TreeItemData> =
        TreeTableColumn<TreeItemData, TreeItemData>().apply {
            text = JsonPropertiesMl.bundle.getString("jsonEditor.keyCell")
            cellValueFactory = Callback { it.value.valueProperty() }
            cellFactory = Callback { KeyCell() }
            styleClass.add("key-cell")
            minWidth = 150.0
            isSortable = false
        }

    inner class KeyCell : TreeTableCell<TreeItemData, TreeItemData>() {
        private var changeListener: ((TreeItemData) -> Unit) = this::updateUi

        init {
            styleClass.add("key-cell")
        }

        public override fun updateItem(item: TreeItemData?, empty: Boolean) {
            getItem()?.removeChangeListener(changeListener)
            super.updateItem(item, empty)

            if (item == null || empty) {
                text = null
                graphic = null
            } else {
                updateUi(item)
                item.registerChangeListener(changeListener)
            }
        }

        fun updateUi(treeItemData: TreeItemData) {
            updateLabel(treeItemData)
            updateValidation(treeItemData)
            updateTooltip(treeItemData)
        }

        private fun updateLabel(treeItemData: TreeItemData) {
            graphic = StackPane(Label().apply {
                StackPane.setAlignment(this, Pos.CENTER_LEFT)
                text =
                    treeItemData.title + if (viewOptions.markRequired && treeItemData.required) " *" else ""
                treeItemData.titleMenu?.also { menu ->
                    isUnderline = true
                    cursor = Cursor.HAND
                    var oldMenu: ContextMenu? = null
                    setOnMouseClicked { e ->
                        oldMenu?.hide()
                        oldMenu = menu().also { it.show(this, e.screenX, e.screenY) }
                    }
                }
            }
            )
        }

        private fun updateTooltip(treeItemData: TreeItemData) {
            val desc = treeItemData.description
            val validationMessage = treeItemData.validationMessage

            if (desc != null) {
                tooltip = Tooltip().apply {
                    styleClass.add("json-props-editor-tooltip")
                    contentDisplay = ContentDisplay.BOTTOM

                    text = desc

                    validationMessage?.let {
                        graphic = Label(it.message).apply {
                            styleClass.add(
                                when (it.severity) {
                                    Severity.ERROR -> "error-display"
                                    Severity.WARNING -> "warning-display"
                                    Severity.OK -> "ok-display"
                                    else -> "info-display"
                                }
                            )
                        }
                    }
                }
            } else {
                tooltip = null
            }
        }

        private fun updateValidation(treeItemData: TreeItemData) {
            (graphic as? StackPane)?.also { pane ->
                DECORATOR.removeDecorations(pane)
                val msg = treeItemData.validationMessage
                if (msg != null) Platform.runLater {
                    DECORATOR.applyValidationDecoration(pane, msg)
                }
            }
        }

        private fun createValidationMessage(
            label: Control,
            msg: Validator.ValidationResult?
        ): SimpleValidationMessage? =
            msg?.let { SimpleValidationMessage(label, it.message, it.severity) }

        private inner class SimpleValidationMessage(
            private val target: Control,
            private val text: String,
            private val severity: Severity
        ) : ValidationMessage {
            override fun getTarget(): Control = target
            override fun getText(): String = text
            override fun getSeverity(): Severity = severity
        }
    }

    private fun createControlColumn(): TreeTableColumn<TreeItemData, TreeItemData> =
        TreeTableColumn<TreeItemData, TreeItemData>().apply {
            text = JsonPropertiesMl.bundle.getString("jsonEditor.valueCell")
            cellValueFactory = Callback { it.value.valueProperty() }
            cellFactory = Callback { ValueCell() }
            minWidth = 150.0
            styleClass.add("control-cell")
            isSortable = false
        }

    class ValueCell : TreeTableCell<TreeItemData, TreeItemData>() {
        private var changeListener: ((TreeItemData) -> Unit) = this::updateControl
        private var lazyControl: LazyControl? = null

        // if we don't delay this, we run into issues when collapsing a cell that is currently focused
        // but with the delay, it sometimes happens that the control isn't actually focused anymore by the time we get to the runLater, so we check again
        private val focusListener =
            ChangeListener { obs, _, new: Boolean ->
                if (new) {
                    Platform.runLater {
                        if (obs.value) {
                            treeTableView.selectionModel.select(tableRow.index, tableColumn)
                        }
                    }

                }
            }

        init {
            selectedProperty().addListener { obs, _, newValue ->
                if (newValue) {
                    Platform.runLater {
                        if (obs.value) {
                            lazyControl?.control?.requestFocus()
                        }
                    }

                }
            }
        }

        public override fun updateItem(item: TreeItemData?, empty: Boolean) {
            getItem()?.removeChangeListener(changeListener)
            super.updateItem(item, empty)

            lazyControl?.control?.focusedProperty()?.removeListener(focusListener)

            lazyControl = item?.createControl()
            if (item == null || empty) {
                text = null
                graphic = null
            } else {
                graphic = lazyControl?.control?.also {
                    it.focusedProperty().addListener(focusListener)
                }
                updateControl(item)

                item.registerChangeListener(changeListener)
            }
        }

        private fun updateControl(item: TreeItemData?) {
            lazyControl?.also { it.updateDisplayedValue() }
        }
    }

    private fun createActionColumn(): TreeTableColumn<TreeItemData, TreeItemData> =
        TreeTableColumn<TreeItemData, TreeItemData>().apply {
            text = JsonPropertiesMl.bundle.getString("jsonEditor.actionCell")
            cellValueFactory = Callback { it.value.valueProperty() }
            cellFactory = Callback { ActionCell() }
            minWidth = 100.0
            prefWidth = 100.0
            styleClass.add("action-cell")
            style = "-fx-alignment: CENTER"
            isSortable = false
        }

    fun addFilter(filter: ItemFilter) {
        filters.add(filter)
        refreshFilter()
    }

    fun removeFilter(filter: ItemFilter) {
        filters.remove(filter)
        refreshFilter()
    }

    fun replaceFilter(
        oldFilter: ItemFilter?,
        newFilter: ItemFilter?
    ) {
        if (oldFilter != null) {
            filters.remove(oldFilter)
        }
        if (newFilter != null) {
            filters.add(newFilter)
        }
        refreshFilter()
    }

    private fun refreshFilter() {
        if (filters.isEmpty()) {
            rootItem.setFilter { true }
        } else {
            rootItem.setFilter { item ->
                (item as? ControlTreeItemData)?.let {
                    filters.any { !it(item.typeControl.model, item.validationMessage?.message) }
                } ?: true
            }
        }

    }

    /**
     * The callback that is being executed after the user changed something.
     */
    fun interface OnEditCallback : (PropertiesEditInput) -> PropertiesEditResult

    /**
     * A filter that can be used to filter the displayed objects and fields.
     */
    fun interface ItemFilter : (TypeModel<*, *>, String?) -> Boolean {
        override fun invoke(model: TypeModel<*, *>, validationMessage: String?): Boolean
    }

    class ActionCell : TreeTableCell<TreeItemData, TreeItemData>() {
        private var changeListener: ((TreeItemData) -> Unit) = this::updateActionEnablement
        private var actions: ActionsContainer? = null
        override fun updateItem(item: TreeItemData?, empty: Boolean) {
            getItem()?.removeChangeListener(changeListener)
            super.updateItem(item, empty)

            val node = item?.createActions()
            if (item == null || empty) {
                text = null
                graphic = null
            } else {
                graphic = node
                actions = node
                updateActionEnablement(item)
                item.registerChangeListener(changeListener)
            }
        }

        private fun updateActionEnablement(item: TreeItemData) {
            actions?.updateDisablement()
        }
    }

    class TreeItemDataRow : TreeTableRow<TreeItemData>() {
        private val appliedStyleClasses = mutableListOf<String>()
        override fun updateItem(item: TreeItemData?, empty: Boolean) {
            super.updateItem(item, empty)
            cursor = null
            styleClass.removeAll(appliedStyleClasses)
            appliedStyleClasses.clear()
            if (item != null) {
                appliedStyleClasses.addAll(item.cssClasses)
                val indent = calcIndent()
                if (indent >= 0) {
                    appliedStyleClasses.add(
                        if (indent % 2 == 0) "json-props-editor-even-indent-row"
                        else "json-props-editor-odd-indent-row"
                    )
                }
                styleClass.addAll(appliedStyleClasses)
//                style = item.cssStyle
            }
        }

        private fun calcIndent(): Int {
            var indent = -1
            var current = treeItem
            while (current != null) {
                indent++
                current = current.parent
            }
            return indent
        }
    }

    companion object {
        val DECORATOR = ExtendedGraphicValidationDecoration()
    }
}