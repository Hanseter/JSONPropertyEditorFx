package com.github.hanseter.json.editor.controls

import javafx.scene.control.TextField
import org.everit.json.schema.StringSchema
import org.controlsfx.validation.ValidationSupport
import org.everit.json.schema.FormatValidator
import org.controlsfx.validation.ValidationResult
import org.controlsfx.validation.Validator
import java.util.regex.Pattern
import org.controlsfx.validation.Severity
import org.json.JSONObject
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.beans.property.Property
import com.github.hanseter.json.editor.extensions.SchemaWrapper

class StringControl(schema: SchemaWrapper<StringSchema>) :
	RowBasedControl<StringSchema, String, TextField>(
		schema,
		TextField(),
		{ it.textProperty() },
		schema.schema.getDefaultValue() as? String ?: ""
	),
	ChangeListener<String> {
	val validation = ValidationSupport()
	val formatValidator: Validator<String>?
	val lengthValidator: Validator<String>?
	val patternValidator: Validator<String>?

	init {
		formatValidator = addFormatValidation(validation, control, schema.schema.getFormatValidator())
		lengthValidator =
			addLengthValidation(validation, control, schema.schema.getMinLength(), schema.schema.getMaxLength())
		patternValidator = addPatternValidation(validation, control, schema.schema.getPattern())
	}

	companion object {
		fun addFormatValidation(
			validation: ValidationSupport,
			textField: TextField,
			formatValidator: FormatValidator?
		): Validator<String>? {
			val validator = createFormatValidation(formatValidator)
			if (validator == null) return null
			validation.registerValidator(textField, validator)
			return validator
		}

		fun createFormatValidation(format: FormatValidator?): Validator<String>? = if (format == null) {
			null
		} else {
			Validator({ control, value: String ->
				val validationResult = format.validate(value).orElse(null)
				ValidationResult.fromErrorIf(
					control,
					validationResult,
					validationResult != null
				)
			})
		}

		fun addLengthValidation(
			validation: ValidationSupport,
			textField: TextField,
			minLength: Int?,
			maxLength: Int?
		): Validator<String>? {
			val validator = createLengthValidation(minLength, maxLength)
			if (validator == null) return null
			validation.registerValidator(textField, validator)
			return validator
		}

		fun createLengthValidation(minLength: Int?, maxLength: Int?): Validator<String>? = when {
			minLength != null && maxLength != null -> Validator({ control, value: String ->
				ValidationResult.fromErrorIf(
					control,
					"Has to be $minLength to $maxLength characters",
					value.length < minLength || value.length > maxLength
				)
			})
			minLength != null -> Validator({ control, value: String ->
				ValidationResult.fromErrorIf(
					control,
					"Has to be at max $minLength characters",
					value.length < minLength
				)
			})
			maxLength != null -> Validator({ control, value: String ->
				ValidationResult.fromErrorIf(
					control,
					"Has to be at least $maxLength characters",
					value.length > maxLength
				)
			})
			else -> null
		}

		fun addPatternValidation(
			validation: ValidationSupport,
			textField: TextField,
			pattern: Pattern?
		): Validator<String>? {
			if (pattern == null) return null
			val validator = Validator.createRegexValidator("Has to match pattern ${pattern}", pattern, Severity.ERROR)
			validation.registerValidator(textField, validator)
			return validator
		}
	}
}