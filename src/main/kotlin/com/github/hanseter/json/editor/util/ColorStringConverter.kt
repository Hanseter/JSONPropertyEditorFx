package com.github.hanseter.json.editor.util

import javafx.scene.paint.Color
import javafx.util.StringConverter
import kotlin.math.roundToInt

object ColorStringConverter : StringConverter<Color>() {
    override fun toString(color: Color?): String? {
        if (color == null) return null
        val r = (color.red * 255.0).roundToInt()
        val g = (color.green * 255.0).roundToInt()
        val b = (color.blue * 255.0).roundToInt()
        val opacity = (color.opacity * 255.0).roundToInt()
        return "#%02X%02X%02X%02X".format(r, g, b, opacity)
    }

    override fun fromString(string: String?): Color? {
        if (string == null) return null
        return Color.web(string)
    }
}