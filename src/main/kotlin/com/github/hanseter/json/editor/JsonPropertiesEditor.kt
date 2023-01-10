package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.actions.*
import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import com.github.hanseter.json.editor.types.TypeModel
import com.github.hanseter.json.editor.ui.*
import com.github.hanseter.json.editor.util.CustomizationObject
import com.github.hanseter.json.editor.util.DefaultCustomizationObject
import com.github.hanseter.json.editor.util.LazyControl
import com.github.hanseter.json.editor.util.ViewOptions
import com.github.hanseter.json.editor.validators.IdReferenceValidator
import com.github.hanseter.json.editor.validators.Validator
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableBooleanValue
import javafx.beans.value.ObservableValue
import javafx.beans.value.WeakChangeListener
import javafx.scene.control.*
import javafx.scene.layout.StackPane
import javafx.util.Callback
import org.controlsfx.validation.Severity
import org.controlsfx.validation.ValidationMessage
import org.controlsfx.validation.decoration.GraphicValidationDecoration
import org.json.JSONObject
import java.net.URI

class JsonPropertiesEditor @JvmOverloads constructor(
    private val readOnly: Boolean = false,
    viewOptions: ViewOptions = ViewOptions(),
    actions: List<EditorAction> = listOf(ResetToDefaultAction, ResetToNullAction),
    private val customizationObject: CustomizationObject = DefaultCustomizationObject
) : StackPane() {
    var referenceProposalProvider: IdReferenceProposalProvider =
        IdReferenceProposalProvider.IdReferenceProposalProviderEmpty
    var resolutionScopeProvider: ResolutionScopeProvider =
        ResolutionScopeProvider.ResolutionScopeProviderEmpty
    val validators: List<Validator> =
        listOf(IdReferenceValidator { referenceProposalProvider })

    private val actions =
        actions + PreviewAction(
            { referenceProposalProvider },
            { resolutionScopeProvider }) + arrayActions

    private val idsToPanes = mutableMapOf<String, JsonPropertiesPane>()
    private val rootItem: FilterableTreeItem<TreeItemData> =
        FilterableTreeItem(StyledTreeItemData("root", listOf()))

    private val keyColumn = createKeyColumn()

    private val treeTableView = TreeTableView<TreeItemData>().apply {
        id = "itemTable"
        rowFactory = Callback { TreeItemDataRow() }

        stylesheets.add(
            this@JsonPropertiesEditor.javaClass
                .getResource("TreeTableView.css")!!
                .toExternalForm()
        )
        columns.addAll(keyColumn, createControlColumn(), createActionColumn())
        isShowRoot = false
        columnResizePolicy = TreeTableView.CONSTRAINED_RESIZE_POLICY
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
    val valid: ReadOnlyBooleanProperty
        get() = _valid
    var viewOptions: ViewOptions = viewOptions
        set(value) {
            field = value
            idsToPanes.values.forEach { it.viewOptions = value }
        }

    private val filters = mutableListOf<ItemFilter>()

    init {
        this.children.addAll(scrollPane)
    }

    fun display(
        objId: String,
        title: String,
        obj: JSONObject,
        schema: JSONObject,
        callback: OnEditCallback
    ) {
        if (idsToPanes.contains(objId)) {
            updateObject(objId, obj)
            return
        }

        val resolutionScope = resolutionScopeProvider.getResolutionScopeForElement(objId)

        val pane = createTitledPaneForSchema(
            title, objId, obj,
            schema,
            readOnly,
            resolutionScope, customizationObject, callback
        )
        pane.fillData(obj)
        idsToPanes[objId] = pane
        (treeTableView.root as FilterableTreeItem).add(pane.treeItem)
        pane.treeItem.isExpanded = idsToPanes.size <= viewOptions.numberOfInitiallyOpenedObjects
        rebindValidProperty()
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

    fun updateObject(
        objId: String,
        obj: JSONObject
    ) {
        val pane = idsToPanes[objId] ?: return
        pane.fillData(obj)
    }

    fun removeObject(objId: String) {
        (idsToPanes.remove(objId)?.treeItem)?.also {
            (treeTableView.root as FilterableTreeItem).remove(
                it
            )
        }
        rebindValidProperty()
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
        idsToPanes.clear()
        (treeTableView.root as FilterableTreeItem).clear()
        rebindValidProperty()
    }

    private fun rebindValidProperty() {
        if (idsToPanes.isEmpty()) {
            _valid.unbind()
            _valid.set(true)
        } else {
            _valid.bind(idsToPanes.values.map { it.valid as ObservableBooleanValue }
                .reduce { a, b -> Bindings.and(a, b) })
        }
    }


    private fun createTitledPaneForSchema(
        title: String, objId: String, data: JSONObject,
        rawSchema: JSONObject, readOnly: Boolean, resolutionScope: URI?,
        customizationObject: CustomizationObject,
        callback: OnEditCallback
    ): JsonPropertiesPane =
        JsonPropertiesPane(
            title,
            objId,
            data,
            rawSchema,
            readOnly,
            resolutionScope,
            { referenceProposalProvider },
            actions,
            validators,
            viewOptions,
            customizationObject,
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

        fun updateLabel(treeItemData: TreeItemData) {
            graphic = Label().apply {
                text =
                    treeItemData.title + if (viewOptions.markRequired && treeItemData.required) " *" else ""

                skin = DecoratableLabelSkin(this)
            }
        }

        fun updateTooltip(treeItemData: TreeItemData) {
            val desc = treeItemData.description
            val validationMessage = treeItemData.validationMessage

            if (desc != null) {
                tooltip = Tooltip().apply {
                    styleClass.add("json-props-editor-tooltip")
                    contentDisplay = ContentDisplay.BOTTOM

                    text = desc

                    validationMessage?.let {
                        graphic = Label(it).apply {
                            styleClass.add("error-display")
                        }
                    }
                }
            } else {
                tooltip = null
            }
        }

        fun updateValidation(treeItemData: TreeItemData) {
            (graphic as? Control)?.also { label ->
                DECORATOR.removeDecorations(label)
                createValidationMessage(
                    label,
                    treeItemData.validationMessage
                )?.also(DECORATOR::applyValidationDecoration)
            }
        }

        private fun createValidationMessage(
            label: Control,
            msg: String?
        ): SimpleValidationMessage? =
            msg?.let { SimpleValidationMessage(label, it, Severity.ERROR) }

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
                            println("Focus received ${treeTableRow.index}, ${treeTableView.expandedItemCount}")
                            treeTableView.selectionModel.select(treeTableRow.index, tableColumn)
                        }
                    }

                }
            }

        init {
            selectedProperty().addListener { obs, _, newValue ->
                if (newValue) {
                    Platform.runLater {
                        if (obs.value) {
                            println("Cell selected ${treeTableRow.index}, ${treeTableView.expandedItemCount}")
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
                    filters.any { !it(item.typeControl.model, item.validationMessage) }
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
                styleClass.addAll(appliedStyleClasses)
                style = item.cssStyle
            }
        }
    }

    companion object {
        val DECORATOR = GraphicValidationDecoration()
    }
}