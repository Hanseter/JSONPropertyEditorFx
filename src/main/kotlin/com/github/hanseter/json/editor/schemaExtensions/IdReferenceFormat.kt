package com.github.hanseter.json.editor.schemaExtensions

import java.util.Optional

object IdReferenceFormat {
	const val formatName = "id-reference"

	object Validator : org.everit.json.schema.FormatValidator {

		override fun formatName() = formatName

		override fun validate(subject: String): Optional<String> =
			Optional.empty()
	}
}