package com.github.hanseter.json.editor.schemaExtensions

import com.github.hanseter.json.editor.ui.TIME_REGEX
import java.util.*

object LocalTimeFormat {
    val format = TIME_REGEX
    const val formatName = "local-time"

    object Validator : org.everit.json.schema.FormatValidator {

        override fun formatName() = formatName

        override fun validate(subject: String?): Optional<String> {
            if (subject == null) return Optional.empty()
            return if (format.matches(subject)) Optional.empty()
            else Optional.of("Has to be a valid time (e.g. 15:23:46)")
        }
    }
}