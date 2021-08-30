package com.github.hanseter.json.editor.controls

import javafx.beans.property.Property
import javafx.scene.control.DatePicker
import javafx.util.StringConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DateControl : ControlWithProperty<String?> {
    override val control: DatePicker =
            DatePicker().apply { converter = StringDateConverter}
    override val property: Property<String?>
        get() = control.editor.textProperty()


    override fun previewNull(b: Boolean) {
        control.promptText = if (b) TypeControl.NULL_PROMPT else ""
    }
}

object StringDateConverter : StringConverter<LocalDate>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    override fun toString(`object`: LocalDate?): String? =
            `object`?.let { formatter.format(it) }

    override fun fromString(string: String?): LocalDate? =
            string?.let { LocalDate.from(formatter.parse(it)) }

}