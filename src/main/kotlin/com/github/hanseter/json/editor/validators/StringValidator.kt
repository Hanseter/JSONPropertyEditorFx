package com.github.hanseter.json.editor.validators

import org.everit.json.schema.FormatValidator
import org.everit.json.schema.StringSchema
import java.util.regex.Pattern

class StringValidator(schema: StringSchema) : Validator<String?> {
    val validators = buildStringValidators(schema)

    override fun validate(value: String?): List<String> = validators.flatMap { it.validate(value) }
}

fun buildStringValidators(schema: StringSchema): List<Validator<String?>> {
    val validators = mutableListOf<Validator<String?>>()
    createLengthValidation(schema.minLength, schema.maxLength)?.also { validators.add(it) }
    createFormatValidation(schema.formatValidator)?.also { validators.add(it) }
    createPatternValidation(schema.pattern)?.also { validators.add(it) }
    return validators
}

fun createLengthValidation(minLength: Int?, maxLength: Int?): Validator<String?>? = when {
    minLength != null && maxLength != null -> Validator { value ->
        if (value != null && (value.length < minLength || value.length > maxLength)) listOf("Has to be $minLength to $maxLength characters")
        else listOf()
    }
    minLength != null -> Validator { value ->
        if (value != null && value.length < minLength) listOf("Has to be at least $minLength characters")
        else listOf()
    }
    maxLength != null -> Validator { value ->
        if (value != null && value.length > maxLength) listOf("Has to be at most $maxLength characters")
        else listOf()
    }
    else -> null
}

fun createFormatValidation(format: FormatValidator?): Validator<String?>? =
        if (format == null) null
        else Validator { value ->
            value?.let { format.validate(value).map { listOf(it) }.orElse(listOf()) } ?: listOf()
        }

fun createPatternValidation(pattern: Pattern?): Validator<String?>? = when (pattern) {
    null -> null
    else -> Validator { value ->
        if (value != null && !pattern.matcher(value).matches()) listOf("Has to match pattern $pattern")
        else listOf()
    }
}