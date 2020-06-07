package com.github.hanseter.json.editor.controls

import org.everit.json.schema.StringSchema
import javafx.beans.value.ChangeListener
import org.json.JSONObject
import javafx.beans.value.ObservableValue
import javafx.beans.property.Property
import javafx.scene.control.ColorPicker
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.paint.Color
import javafx.beans.property.SimpleStringProperty
import javafx.beans.binding.Bindings
import javafx.util.StringConverter
import com.github.hanseter.json.editor.extensions.SchemaWrapper

class ColorControl(schema: SchemaWrapper<StringSchema>) :
	RowBasedControl<StringSchema, String, ColorPicker>(
		schema,
		ColorPicker(),
		SimpleStringProperty("#FFFFFFFF"),
		schema.schema.getDefaultValue() as? String
	) {

	init {
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