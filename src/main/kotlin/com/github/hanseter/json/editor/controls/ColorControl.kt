package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.actions.EditorAction
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ColorPicker
import javafx.scene.paint.Color
import javafx.util.StringConverter
import org.everit.json.schema.StringSchema

class ColorControl(schema: SchemaWrapper<StringSchema>, actions: List<EditorAction>) :
        RowBasedControl<StringSchema, String, ColorPicker>(
                schema,
                ColorPicker(),
                SimpleStringProperty("#FFFFFFFF"),
                schema.schema.defaultValue as? String,
                actions
        ) {

    init {
        control.minHeight = 25.0
        control.minWidth = 150.0
        Bindings.bindBidirectional(value, control.valueProperty(), ColorStringConverter)
    }

    object ColorStringConverter : StringConverter<Color>() {
        override fun toString(color: Color?): String {
            if (color == null) return "#FFFFFFFF"
            val r = Math.round(color.getRed() * 255.0).toInt()
            val g = Math.round(color.getGreen() * 255.0).toInt()
            val b = Math.round(color.getBlue() * 255.0).toInt()
            val opacity = Math.round(color.getOpacity() * 255.0).toInt()
            return "#%02X%02X%02X%02X".format(r, g, b, opacity)
        }

        override fun fromString(string: String?): Color {
            if (string == null) return Color.WHITE
            return Color.web(string)
        }
    }
}