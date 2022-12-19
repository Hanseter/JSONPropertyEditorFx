package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.SimpleEffectiveSchema
import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import com.github.hanseter.json.editor.types.OneOfModel
import com.github.hanseter.json.editor.types.PreviewString
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.LazyControl
import javafx.beans.value.ChangeListener
import javafx.scene.control.ComboBox
import javafx.util.StringConverter
import org.everit.json.schema.Schema

class OneOfControl(override val model: OneOfModel) : TypeControl {
    override val childControls: List<TypeControl>
        get() = model.actualType?.let { listOf(it) } ?: emptyList()

    override fun bindTo(type: BindableJsonType) {
        val child = model.actualType
        val grandChildren = child?.childControls?.toList().orEmpty()
        model.bound = type
        val newChild = model.actualType
        if (newChild !== child || newChild?.childControls.orEmpty() != grandChildren) {
            model.editorContext.childrenChangedCallback(this)
        }
    }

    override fun createLazyControl(): LazyControl = OneOfLazyControl()

    private inner class OneOfLazyControl : LazyControl {
        private val selectionListener: ChangeListener<Schema?> =
            ChangeListener<Schema?> { _, _, selected ->
                model.selectType(selected)
                model.editorContext.childrenChangedCallback(this@OneOfControl)
            }
        override val control: ComboBox<Schema> = ComboBox<Schema>().apply {
            items.addAll(model.schema.baseSchema.subschemas)
            converter = OneOfModel.Companion.SchemaTitleStringConverter
            selectionModel.selectedItemProperty().addListener(selectionListener)

            isDisable = model.schema.readOnly
        }

        override fun updateDisplayedValue() {
            control.selectionModel.selectedItemProperty().removeListener(selectionListener)
            val newChild = model.actualType
            control.selectionModel.select(newChild?.model?.schema?.baseSchema)
            control.selectionModel.selectedItemProperty().addListener(selectionListener)
        }


    }


}