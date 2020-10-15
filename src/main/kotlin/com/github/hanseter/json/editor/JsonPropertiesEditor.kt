package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.actions.EditorAction
import com.github.hanseter.json.editor.actions.ResetToDefaultAction
import com.github.hanseter.json.editor.actions.ResetToNullAction
import com.github.hanseter.json.editor.extensions.CustomNodeTreeTableCell
import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.schemaExtensions.ColorFormat
import com.github.hanseter.json.editor.schemaExtensions.IdReferenceFormat
import javafx.beans.binding.Bindings
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.scene.Cursor
import javafx.scene.control.*
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.util.Callback
import org.everit.json.schema.Schema
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import java.net.URI
import java.util.function.Predicate

class JsonPropertiesEditor(
        private val referenceProposalProvider: IdReferenceProposalProvider = IdReferenceProposalProvider.IdReferenceProposalProviderEmpty,
        private val readOnly: Boolean = false,
        private val numberOfInitiallyOpenedObjects: Int = 5,
        private val resolutionScopeProvider: ResolutionScopeProvider = ResolutionScopeProvider.ResolutionScopeProviderEmpty,
        private val actions: List<EditorAction> = listOf(ResetToDefaultAction, ResetToNullAction)
) : VBox() {
    private val idsToPanes = mutableMapOf<String, JsonPropertiesPane>()
    private val scrollPane = ScrollPane()
    private val filterText = TextField()
    private val treeTableView = TreeTableView<TreeItemData>()
    private val _valid = SimpleBooleanProperty(true)
    val valid: ReadOnlyBooleanProperty
        get() = _valid


    init {
        initTreeTableView()
        filterText.promptText = "Filter properties"

        val filteredTreeItemRoot = FilterableTreeItem(TreeItemData("root", null, null, null, false))
        filterText.textProperty().addListener { _, _, newValue ->
            filteredTreeItemRoot.setPredicate(
                    if (newValue.isEmpty()) {
                        Predicate { true }
                    } else {
                        Predicate { it.key.contains(newValue) }
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
            callback: (JSONObject) -> JSONObject
    ) {
        if (idsToPanes.contains(objId)) {
            updateObject(objId, obj)
            return
        }
        val pane = createTitledPaneForSchema(
                title, obj,
                parseSchema(schema, resolutionScopeProvider.getResolutionScopeForElement(objId)),
                callback
        )
        pane.fillData(obj)
        idsToPanes[objId] = pane
        (treeTableView.root as FilterableTreeItem).add(pane.treeItem)
        pane.treeItem.isExpanded = idsToPanes.size <= numberOfInitiallyOpenedObjects
        rebindValidProperty()
    }

    private fun parseSchema(schema: JSONObject, resolutionScope: URI?): Schema {
        val slb = SchemaLoader.builder()
                .useDefaults(true)
                .draftV7Support()
                .addFormatValidator(ColorFormat.Validator)
                .addFormatValidator(IdReferenceFormat.Validator)
                .schemaJson(schema)

        if (resolutionScope != null) {
            slb.resolutionScope(resolutionScope)
        }

        return slb.build().load().readOnly(readOnly).build()
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

    private fun createTitledPaneForSchema(title: String, data: JSONObject,
                                          schema: Schema, callback: (JSONObject) -> JSONObject): JsonPropertiesPane =
            JsonPropertiesPane(title, data, schema, referenceProposalProvider, resolutionScopeProvider, actions) { obj, pane ->
                pane.fillData(callback(obj))
            }

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

    private fun createKeyColumn(): TreeTableColumn<TreeItemData, String> =
            TreeTableColumn<TreeItemData, String>().apply {
                text = "Key"
                cellValueFactory = Callback { SimpleStringProperty(it.value.value.key) }
                cellFactory = Callback { _ ->
                    object : TreeTableCell<TreeItemData, String>() {
                        override fun updateItem(item: String?, empty: Boolean) {
                            if (item === getItem()) return
                            super.updateItem(item, empty)
                            treeTableRow?.treeItem?.value?.description?.also {
                                tooltip = Tooltip(it)
                            }
                            super.setText(item)
                        }
                    }
                }
                minWidth = 150.0
                isSortable = false
            }

    private fun createControlColumn(): TreeTableColumn<TreeItemData, TreeItemData> =
            TreeTableColumn<TreeItemData, TreeItemData>().apply {
                text = "Value"
                cellValueFactory = Callback { it.value.valueProperty() }
                cellFactory = Callback { _ -> CustomNodeTreeTableCell { it.control } }
                minWidth = 150.0
                styleClass.add("control-cell")
                isSortable = false
            }

    private fun createActionColumn(): TreeTableColumn<TreeItemData, TreeItemData> =
            TreeTableColumn<TreeItemData, TreeItemData>().apply {
                text = "Action"
                cellFactory = Callback { _ -> CustomNodeTreeTableCell { it.action } }
                cellValueFactory = Callback { it.value.valueProperty() }
                minWidth = 100.0
                prefWidth = 100.0
                styleClass.add("action-cell")
                style = "-fx-alignment: CENTER"
                isSortable = false
            }

    class TreeItemDataRow : TreeTableRow<TreeItemData>() {
        override fun updateItem(item: TreeItemData?, empty: Boolean) {
            super.updateItem(item, empty)
            cursor = null
            styleClass.remove("isRootRow")
            styleClass.remove("isHeadlineRow")
            if (item != null && item.isRoot) {
                cursor = Cursor.HAND
                styleClass.add("isRootRow")
                setOnMouseClicked {
                    if (!disclosureNode.boundsInParent.contains(it.x, it.y)) {
                        treeItem.isExpanded = !treeItem.isExpanded
                    }
                }
            } else if (item != null && item.isHeadline) {
                styleClass.add("isHeadlineRow")
                onMouseClicked = null
            } else {
                onMouseClicked = null
            }
        }
    }
}