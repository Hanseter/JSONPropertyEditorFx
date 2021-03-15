package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.actions.*
import com.github.hanseter.json.editor.ui.DecoratableLabelSkin
import com.github.hanseter.json.editor.ui.FilterableTreeItem
import com.github.hanseter.json.editor.ui.StyledTreeItemData
import com.github.hanseter.json.editor.ui.TreeItemData
import com.github.hanseter.json.editor.util.LazyControl
import com.github.hanseter.json.editor.util.ViewOptions
import com.github.hanseter.json.editor.validators.IdReferenceValidator
import com.github.hanseter.json.editor.validators.Validator
import javafx.beans.binding.Bindings
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.scene.control.*
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.util.Callback
import org.controlsfx.validation.Severity
import org.controlsfx.validation.ValidationMessage
import org.controlsfx.validation.decoration.GraphicValidationDecoration
import org.everit.json.schema.Schema
import org.json.JSONObject
import java.net.URI
import java.util.function.Predicate

class JsonPropertiesEditor(
        private val referenceProposalProvider: IdReferenceProposalProvider = IdReferenceProposalProvider.IdReferenceProposalProviderEmpty,
        private val readOnly: Boolean = false,
        private val numberOfInitiallyOpenedObjects: Int = 5,
        private val resolutionScopeProvider: ResolutionScopeProvider = ResolutionScopeProvider.ResolutionScopeProviderEmpty,
        viewOptions: ViewOptions = ViewOptions(),
        actions: List<EditorAction> = listOf(ResetToDefaultAction, ResetToNullAction),
        private val validators: List<Validator> = listOf(IdReferenceValidator(referenceProposalProvider))
) : VBox() {
    private val actions = actions + PreviewAction(referenceProposalProvider, resolutionScopeProvider) + arrayActions
    private val idsToPanes = mutableMapOf<String, JsonPropertiesPane>()
    private val scrollPane = ScrollPane().apply { id = "contentArea" }
    private val filterText = TextField().apply { id = "searchField" }
    private val treeTableView = TreeTableView<TreeItemData>().apply {
        id = "itemTable"
    }
    private val _valid = SimpleBooleanProperty(true)
    val valid: ReadOnlyBooleanProperty
        get() = _valid
    var viewOptions: ViewOptions = viewOptions
        set(value) {
            field = value
            idsToPanes.values.forEach { it.viewOptions = value }
        }

    init {
        initTreeTableView()
        filterText.promptText = "Filter properties"

        val filteredTreeItemRoot: FilterableTreeItem<TreeItemData> = FilterableTreeItem(StyledTreeItemData("root", listOf()))
        filterText.textProperty().addListener { _, _, newValue ->
            filteredTreeItemRoot.setPredicate(
                    if (newValue.isEmpty()) {
                        Predicate { true }
                    } else {
                        Predicate { it.title.contains(newValue) }
                    })
        }

        treeTableView.root = filteredTreeItemRoot
        scrollPane.content = treeTableView
        scrollPane.isFitToHeight = true
        scrollPane.isFitToWidth = true
        val stackPane = StackPane(scrollPane)
        this.children.addAll(filterText, stackPane)
        setVgrow(stackPane, Priority.ALWAYS)
        setVgrow(this, Priority.ALWAYS)
    }

    fun display(
            objId: String,
            title: String,
            obj: JSONObject,
            schema: JSONObject,
            callback: (JSONObject) -> JsonEditorData
    ) {
        if (idsToPanes.contains(objId)) {
            updateObject(objId, obj)
            return
        }

        val resolutionScope = resolutionScopeProvider.getResolutionScopeForElement(objId)

        val pane = createTitledPaneForSchema(
                title, objId, obj,
                SchemaNormalizer.parseSchema(schema, resolutionScope, readOnly, referenceProposalProvider),
                readOnly,
                resolutionScope, callback
        )
        pane.fillData(obj)
        idsToPanes[objId] = pane
        (treeTableView.root as FilterableTreeItem).add(pane.treeItem)
        pane.treeItem.isExpanded = idsToPanes.size <= numberOfInitiallyOpenedObjects
        rebindValidProperty()
    }

    fun updateObject(
            objId: String,
            obj: JSONObject
    ) {
        val pane = idsToPanes[objId] ?: return
        pane.fillData(obj)
    }

    fun removeObject(objId: String) {
        (idsToPanes.remove(objId)?.treeItem)?.also { (treeTableView.root as FilterableTreeItem).remove(it) }
        rebindValidProperty()
    }

    fun clear() {
        idsToPanes.clear()
        filterText.clear()
        (treeTableView.root as FilterableTreeItem).clear()
        rebindValidProperty()
    }

    private fun rebindValidProperty() {
        if (idsToPanes.isEmpty()) {
            _valid.unbind()
            _valid.set(true)
        } else {
            _valid.bind(idsToPanes.values.map { it.valid as ObservableBooleanValue }.reduce { a, b -> Bindings.and(a, b) })
        }
    }


    private fun createTitledPaneForSchema(
            title: String, objId: String, data: JSONObject,
            schema: Schema, readOnly: Boolean, resolutionScope: URI?, callback: (JSONObject) -> JsonEditorData
    ): JsonPropertiesPane =
            JsonPropertiesPane(title, objId, data, schema, readOnly, resolutionScope, referenceProposalProvider, actions, validators, viewOptions, callback)

    private fun initTreeTableView() {
        val keyColumn = createKeyColumn()
        val controlColumn = createControlColumn()
        val actionColumn = createActionColumn()

        treeTableView.rowFactory = Callback { TreeItemDataRow() }

        treeTableView.also {
            it.stylesheets.add(javaClass.getResource("TreeTableView.css").toExternalForm())
            it.columns.addAll(keyColumn, controlColumn, actionColumn)
            it.isShowRoot = false
            it.columnResizePolicy = TreeTableView.CONSTRAINED_RESIZE_POLICY
        }
    }

    private fun createKeyColumn(): TreeTableColumn<TreeItemData, TreeItemData> =
            TreeTableColumn<TreeItemData, TreeItemData>().apply {
                text = "Key"
                cellValueFactory = Callback { it.value.valueProperty() }
                cellFactory = Callback { KeyCell() }
                minWidth = 150.0
                isSortable = false
            }

    private inner class KeyCell : TreeTableCell<TreeItemData, TreeItemData>() {
        private var changeListener: ((TreeItemData) -> Unit) = this::updateValidation
        override fun updateItem(item: TreeItemData?, empty: Boolean) {
            getItem()?.removeChangeListener(changeListener)
            super.updateItem(item, empty)

            val node = item?.let { dataItem ->
                Label().apply {
                    text = dataItem.title + if (viewOptions.markRequired && dataItem.required) " *" else ""
                    tooltip = dataItem.description?.let { Tooltip(it) }
                    skin = DecoratableLabelSkin(this)
                }
            }
            if (node == null || empty) {
                text = null
                graphic = null
            } else {
                graphic = node
                updateValidation(item)
                item.registerChangeListener(changeListener)
            }
        }

        fun updateValidation(treeItemData: TreeItemData) {
            (graphic as? Control)?.also { label ->
                DECORATOR.removeDecorations(label)
                createValidationMessage(label, treeItemData.validationMessage)?.also(DECORATOR::applyValidationDecoration)
            }
        }

        private fun createValidationMessage(label: Control, msg: String?): SimpleValidationMessage? =
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
                text = "Value"
                cellValueFactory = Callback { it.value.valueProperty() }
                cellFactory = Callback { ValueCell() }
                minWidth = 150.0
                styleClass.add("control-cell")
                isSortable = false
            }

    class ValueCell : TreeTableCell<TreeItemData, TreeItemData>() {
        private var changeListener: ((TreeItemData) -> Unit) = this::updateControl
        private var lazyControl: LazyControl? = null

        public override fun updateItem(item: TreeItemData?, empty: Boolean) {
            getItem()?.removeChangeListener(changeListener)
            super.updateItem(item, empty)

            lazyControl = item?.createControl()
            if (item == null || empty) {
                text = null
                graphic = null
            } else {
                graphic = lazyControl?.control
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
                text = "Action"
                cellValueFactory = Callback { it.value.valueProperty() }
                cellFactory = Callback { ActionCell() }
                minWidth = 100.0
                prefWidth = 100.0
                styleClass.add("action-cell")
                style = "-fx-alignment: CENTER"
                isSortable = false
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
            }
        }
    }

    companion object {
        val DECORATOR = GraphicValidationDecoration()
    }
}