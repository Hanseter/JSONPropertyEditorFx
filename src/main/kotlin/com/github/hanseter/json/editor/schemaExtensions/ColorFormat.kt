package com.github.hanseter.json.editor.schemaExtensions

import java.util.Optional

object ColorFormat {
	val format = Regex.fromLiteral("^#[0-9A-F]{8}$")
	val formatName = "color"

	object Validator : org.everit.json.schema.FormatValidator {
		
		override fun formatName() = formatName

		override fun validate(subject: String): Optional<String> = if (format.matches(subject))
			Optional.empty() else
			Optional.of("Has to be valid RGBA hex color (e.g. #FF0000FF)")
	}
}