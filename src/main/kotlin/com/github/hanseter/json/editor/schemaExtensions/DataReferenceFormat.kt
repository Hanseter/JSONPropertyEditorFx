package com.github.hanseter.json.editor.schemaExtensions

import java.util.*

object DataReferenceFormat {
    const val formatName = "data-reference"

    object Validator : org.everit.json.schema.FormatValidator {

        override fun formatName() = formatName

        override fun validate(p0: String?): Optional<String> = Optional.empty()
    }
}