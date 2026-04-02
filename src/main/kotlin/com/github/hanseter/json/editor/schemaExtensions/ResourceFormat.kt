package com.github.hanseter.json.editor.schemaExtensions

import com.github.hanseter.json.editor.ResourceProvider
import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import org.everit.json.schema.FormatValidator
import java.util.*

/**
 * A custom format for a resource. A resource typically denotes some kind of file in the file system.
 * To use it a [ResourceProvider] must be provided to the [com.github.hanseter.json.editor.JsonPropertiesEditor].
 */
object ResourceFormat {
    const val formatName = "resource"

    object Validator : FormatValidator {

        override fun formatName() = formatName

        override fun validate(subject: String?): Optional<String> = Optional.empty()
    }
}