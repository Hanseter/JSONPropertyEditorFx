package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.actions.ActionsContainer
import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import javafx.beans.binding.Bindings
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.scene.control.ColorPicker
import javafx.scene.paint.Color
import javafx.util.StringConverter
import org.everit.json.schema.StringSchema
import kotlin.math.roundToInt

class ColorControl(override val schema: SchemaWrapper<StringSchema>, context: EditorContext) : TypeControl, ControlProvider<String> {
    override val control = ColorPicker()
    override val value: Property<String?> = SimpleStringProperty("#FFFFFFFF")
    override val defaultValue: String?
        get() = schema.schema.defaultValue as? String
    override val editorActionsContainer: ActionsContainer = context.createActionContainer(this)
    private val delegate = RowBasedControl(this)
    override val node: FilterableTreeItem<TreeItemData> = delegate.node
    override val valid: ObservableBooleanValue = SimpleBooleanProperty(true)

    // TODO ColorPicker does not support promptText, so the Null Prompt cannot be set easily

    init {
        control.minHeight = 25.0
        control.minWidth = 150.0
        Bindings.bindBidirectional(value, control.valueProperty(), ColorStringConverter)
    }

    override fun bindTo(type: BindableJsonType) {
        delegate.bindTo(type)
    }

    object ColorStringConverter : StringConverter<Color>() {
        override fun toString(color: Color?): String {
            if (color == null) return "#FFFFFFFF"
            val r = (color.red * 255.0).roundToInt()
            val g = (color.green * 255.0).roundToInt()
            val b = (color.blue * 255.0).roundToInt()
            val opacity = (color.opacity * 255.0).roundToInt()
            return "#%02X%02X%02X%02X".format(r, g, b, opacity)
        }

        override fun fromString(string: String?): Color {
            if (string == null) return Color.WHITE
            return Color.web(string)
        }
    }
}