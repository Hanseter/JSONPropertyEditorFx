package com.github.hanseter.json.editor.schemaExtensions

import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import java.util.*

object LocalTimeFormat {
    val FORMAT_REGEX = "([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)".toRegex()
    const val formatName = "local-time"

    object Validator : org.everit.json.schema.FormatValidator {

        override fun formatName() = formatName

        override fun validate(subject: String?): Optional<String> {
            if (subject == null) return Optional.empty()
            return if (FORMAT_REGEX.matches(subject)) Optional.empty()
            else Optional.of(JsonPropertiesMl.bundle.getString("jsonEditor.validators.localTimeFormat"))
        }
    }
}