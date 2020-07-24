package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import javafx.scene.control.TextField
import org.controlsfx.validation.Severity
import org.controlsfx.validation.ValidationResult
import org.controlsfx.validation.ValidationSupport
import org.controlsfx.validation.Validator
import org.everit.json.schema.FormatValidator
import org.everit.json.schema.StringSchema
import java.util.regex.Pattern

class StringControl(schema: SchemaWrapper<StringSchema>) :
	RowBasedControl<StringSchema, String, TextField>(
		schema,
		TextField(),
		{ it.textProperty() },
		schema.schema.defaultValue as? String
	) {
	private val validation = ValidationSupport()
	private val formatValidator: Validator<String>?
	private val lengthValidator: Validator<String>?
	private val patternValidator: Validator<String>?

	init {
		formatValidator = addFormatValidation(validation, control, schema.schema.formatValidator)
		lengthValidator =
			addLengthValidation(validation, control, schema.schema.minLength, schema.schema.maxLength)
		patternValidator = addPatternValidation(validation, control, schema.schema.pattern)
		valid.bind(validation.invalidProperty().not())
	}

	companion object {
		fun addFormatValidation(
			validation: ValidationSupport,
			textField: TextField,
			formatValidator: FormatValidator?
		): Validator<String>? {
			val validator = createFormatValidation(formatValidator) ?: return null
			validation.registerValidator(textField, false, validator)
			return validator
		}

		private fun createFormatValidation(format: FormatValidator?): Validator<String>? = if (format == null) {
			null
		} else {
			Validator { control, value: String? ->
				val validationResult = format.validate(value).orElse(null)
				ValidationResult.fromErrorIf(
						control,
						validationResult,
						validationResult != null
				)
			}
		}

		fun addLengthValidation(
			validation: ValidationSupport,
			textField: TextField,
			minLength: Int?,
			maxLength: Int?
		): Validator<String>? {
			val validator = createLengthValidation(minLength, maxLength) ?: return null
			validation.registerValidator(textField, false, validator)
			return validator
		}

		private fun createLengthValidation(minLength: Int?, maxLength: Int?): Validator<String>? = when {
			minLength != null && maxLength != null -> Validator { control, value: String? ->
				ValidationResult.fromErrorIf(
						control,
						"Has to be $minLength to $maxLength characters",
						value?.length ?: 0 < minLength || value?.length ?: 0 > maxLength
				)
			}
			minLength != null -> Validator { control, value: String? ->
				ValidationResult.fromErrorIf(
						control,
						"Has to be at max $minLength characters",
						value?.length ?: 0 < minLength
				)
			}
			maxLength != null -> Validator { control, value: String? ->
				ValidationResult.fromErrorIf(
						control,
						"Has to be at least $maxLength characters",
						value?.length ?: 0 > maxLength
				)
			}
			else -> null
		}

		fun addPatternValidation(
			validation: ValidationSupport,
			textField: TextField,
			pattern: Pattern?
		): Validator<String>? {
			if (pattern == null) return null
			val validator = Validator.createRegexValidator("Has to match pattern $pattern", pattern, Severity.ERROR)
			validation.registerValidator(textField, false, validator)
			return validator
		}
	}
}