package com.github.hanseter.json.editor.schemaExtensions

import java.util.*

object ColorFormat {
    val format = Regex.fromLiteral("^#[0-9A-F]{8}$")
    const val formatName = "color"

    object Validator : org.everit.json.schema.FormatValidator {

        override fun formatName() = formatName

        override fun validate(subject: String?): Optional<String> {
            if (subject == null) return Optional.empty()
            return if (format.matches(subject)) Optional.empty()
            else Optional.of("Has to be valid RGBA hex color (e.g. #FF0000FF)")
        }
    }
}