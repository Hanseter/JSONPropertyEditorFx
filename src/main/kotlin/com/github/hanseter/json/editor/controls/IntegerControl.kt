package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.actions.ActionsContainer
import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.util.converter.IntegerStringConverter
import org.everit.json.schema.NumberSchema

class IntegerControl(override val schema: SchemaWrapper<NumberSchema>, context: EditorContext) : TypeControl, ControlProvider<Int> {
    override val control = Spinner<Int?>(IntegerSpinnerValueFactoryNullSafe(
            schema.schema.minimum?.toInt()
                    ?: (schema.schema.exclusiveMinimumLimit?.toInt()?.inc())
                    ?: Int.MIN_VALUE,
            schema.schema.maximum?.toInt()
                    ?: (schema.schema.exclusiveMaximumLimit?.toInt()?.dec())
                    ?: Int.MAX_VALUE))
    override val value = SimpleObjectProperty<Int>(null)
    override val defaultValue: Int?
        get() = schema.schema.defaultValue as? Int
    override val editorActionsContainer: ActionsContainer = context.createActionContainer(this)

    private val delegate = RowBasedControl(this)

    override val node: FilterableTreeItem<TreeItemData> = delegate.node
    override val valid: ObservableBooleanValue = SimpleBooleanProperty(true)

    init {
        control.isEditable = true
        control.editor.textProperty().addListener { _, _, new ->
            if (new.isEmpty() && !delegate.isRequired) {
                control.increment(0)
            } else if (new.isNotEmpty() && new != "-") {
                try {
                    control.increment(0) // won't change value, but will commit editor
                } catch (e: NumberFormatException) {
                    control.editor.text = control.valueFactory.value?.toString() ?: "0"
                }
            }
        }
        control.focusedProperty().addListener { _, _, new ->
            if (!new && ((control.editor.text.isEmpty() && delegate.isRequired) || control.editor.text == "-")) {
                control.editor.text = control.valueFactory.value?.toString() ?: ""
            }

        }
    }

    override fun bindTo(type: BindableJsonType) {
        delegate.bindTo(type)
        control.editor.promptText = if (delegate.isBoundToNull()) TypeControl.NULL_PROMPT else ""
    }

    class IntegerSpinnerValueFactoryNullSafe(min: Int, max: Int) : SpinnerValueFactory<Int?>() {
        init {
            converter = IntegerStringConverter()

            valueProperty().addListener { _, _, newValue: Int? ->
                if (newValue != null) {
                    if (newValue < min) {
                        value = min
                    } else if (newValue > max) {
                        value = max
                    }
                }
            }
        }

        override fun increment(steps: Int) {
            value = value?.plus(steps)
        }

        override fun decrement(steps: Int) {
            value = value?.minus(steps)
        }
    }
}