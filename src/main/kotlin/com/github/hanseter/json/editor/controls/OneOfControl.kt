package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.RegularSchemaWrapper
import com.github.hanseter.json.editor.types.OneOfModel
import com.github.hanseter.json.editor.util.BindableJsonType
import javafx.beans.value.ChangeListener
import javafx.scene.control.ComboBox
import javafx.util.StringConverter
import org.everit.json.schema.Schema

class OneOfControl(override val model: OneOfModel) : TypeControl {
    private val selectionListener: ChangeListener<Schema?> = ChangeListener<Schema?> { _, _, selected ->
        model.selectType(selected)
        model.editorContext.childrenChangedCallback(this)
    }
    override val control: ComboBox<Schema> = ComboBox<Schema>().apply {
        items.addAll(model.schema.schema.subschemas)
        converter = SchemaTitleStringConverter
        selectionModel.selectedItemProperty().addListener(selectionListener)
    }
    override val childControls: List<TypeControl>
        get() = model.actualType?.let { listOf(it) } ?: emptyList()

    override fun bindTo(type: BindableJsonType) {
        control.selectionModel.selectedItemProperty().removeListener(selectionListener)
        val child = model.actualType
        model.bound = type
        val newChild = model.actualType
        if (newChild !== child) {
            control.selectionModel.select(newChild?.model?.schema?.schema)
            model.editorContext.childrenChangedCallback(this)
        }
        control.selectionModel.selectedItemProperty().addListener(selectionListener)
    }

    private object SchemaTitleStringConverter : StringConverter<Schema>() {
        override fun toString(obj: Schema?): String? =
                obj?.let { RegularSchemaWrapper.calcSchemaTitle(it) }

        override fun fromString(string: String?): Schema? = null
    }
}