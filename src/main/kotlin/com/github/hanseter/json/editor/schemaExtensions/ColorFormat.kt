package com.github.hanseter.json.editor.schemaExtensions

import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import java.util.*

/**
 * A custom format that makes sure a string is in the format of a hexadecimal color with transparency.
 * Using this format also results in a dedicated control in the UI.
 */
object ColorFormat {
    val FORMAT_REGEX = Regex("^#[0-9A-F]{8}$")
    const val formatName = "color"

    object Validator : org.everit.json.schema.FormatValidator {

        override fun formatName() = formatName

        override fun validate(subject: String?): Optional<String> {
            if (subject == null) return Optional.empty()
            return if (FORMAT_REGEX.matches(subject)) Optional.empty()
            else Optional.of(JsonPropertiesMl.bundle.getString("jsonEditor.validators.colorFormat"))
        }
    }
}