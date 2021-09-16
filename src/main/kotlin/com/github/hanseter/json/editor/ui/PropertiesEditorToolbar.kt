package com.github.hanseter.json.editor.ui

import com.github.hanseter.json.editor.JsonPropertiesEditor
import javafx.scene.control.Button
import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.controlsfx.control.textfield.TextFields

class PropertiesEditorToolbar(
    private val editor: JsonPropertiesEditor
) {

    private val options = listOf(
        FilterOption(false, "only required") { model, _ -> model.schema.required },
        FilterOption(false, "only errors") { _, validationMessage -> validationMessage != null }
    )

    val additionalOptions = ArrayList<FilterOption>()

    private val filterText = TextFields.createClearableTextField().apply {
        id = "searchField"
        promptText = ""
        textProperty().addListener { _ ->
            updateFilter()
        }
        HBox.setHgrow(this, Priority.ALWAYS)
    }

    private var caseSensitive = false

    private val optionsButton = Button("⚙").apply {
        val contextMenu = ContextMenu()
        setOnMouseClicked { e ->
            contextMenu.items.setAll(
                listOf(MenuItem("case sensitive", createLabel(caseSensitive)).apply {
                    setOnAction {
                        caseSensitive = !caseSensitive
                        updateFilter()
                    }
                }) + options.map(this@PropertiesEditorToolbar::createMenuItem)
                        + additionalOptions.map(this@PropertiesEditorToolbar::createMenuItem)
            )
            contextMenu.show(this, e.screenX, e.screenY)
        }
    }

    private fun createMenuItem(option: FilterOption) =
        MenuItem(option.description, createLabel(option.active)).apply {
            setOnAction {
                option.active = !option.active
                updateFilter()
            }
        }

    val node = HBox(filterText, optionsButton)

    private var currentFilter: JsonPropertiesEditor.ItemFilter? = null

    private fun createLabel(flag: Boolean) = if (flag) Label("✔") else Label(" ")
    private fun updateFilter() {
        val textFilter = when {
            filterText.text.isEmpty() -> null
            else -> JsonPropertiesEditor.ItemFilter { it, _ ->
                it.schema.title.contains(
                    filterText.text,
                    !caseSensitive
                )
            }
        }

        val filters =
            (listOf(textFilter)
                    + options.map { it.getFilter() }
                    + additionalOptions.map { it.getFilter() }
                    ).filterNotNull()
        val newFilter = if (filters.isEmpty()) null else JsonPropertiesEditor.ItemFilter { a, b ->
            filters.any { !it(a, b) }
        }
        editor.replaceFilter(currentFilter, newFilter)
        currentFilter = newFilter

    }

    /**
     * A filter that can be activated or deactivated by the user.
     * @param active Whether the filter is active
     * @param description A short description for the user what the filter does
     * @param filter The filter logic
     */
    class FilterOption(
        var active: Boolean,
        val description: String,
        private val filter: JsonPropertiesEditor.ItemFilter
    ) {
        fun getFilter() =
            if (active) filter
            else null
    }

}