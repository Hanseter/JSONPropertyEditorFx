package com.github.hanseter.json.editor.schemaExtensions

import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import java.util.*

object ColorFormat {
    val format = Regex("^#[0-9A-F]{8}$")
    const val formatName = "color"

    object Validator : org.everit.json.schema.FormatValidator {

        override fun formatName() = formatName

        override fun validate(subject: String?): Optional<String> {
            if (subject == null) return Optional.empty()
            return if (format.matches(subject)) Optional.empty()
            else Optional.of(JsonPropertiesMl.bundle.getString("jsonEditor.validators.colorFormat"))
        }
    }
}