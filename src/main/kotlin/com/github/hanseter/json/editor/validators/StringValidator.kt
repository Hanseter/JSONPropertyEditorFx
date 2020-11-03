package com.github.hanseter.json.editor.validators

import com.github.hanseter.json.editor.actions.TargetSelector
import com.github.hanseter.json.editor.types.TypeModel
import org.everit.json.schema.FormatValidator
import org.everit.json.schema.StringSchema
import java.util.regex.Pattern

object StringValidator : Validator {
    override val selector: TargetSelector = TargetSelector { it.schema.schema is StringSchema }

    override fun validate(model: TypeModel<*, *>): List<String> =
            validateString(model.schema.schema as StringSchema, model.value as? String)
}

fun validateString(schema: StringSchema, value: String?): List<String> {
    if (value == null) return emptyList()
    val result = mutableListOf<String>()
    validateLength(schema.minLength, schema.maxLength, value)?.also { result.add(it) }
    validateFormat(schema.formatValidator, value)?.also { result.add(it) }
    validatePattern(schema.pattern, value)?.also { result.add(it) }
    return result
}

fun validateLength(minLength: Int?, maxLength: Int?, value: String): String? = when {
    minLength != null && maxLength != null ->
        if ((value.length < minLength || value.length > maxLength)) "Has to be $minLength to $maxLength characters"
        else null
    minLength != null ->
        if (value.length < minLength) "Has to be at least $minLength characters"
        else null
    maxLength != null ->
        if (value.length > maxLength) "Has to be at most $maxLength characters"
        else null
    else -> null
}

fun validateFormat(format: FormatValidator?, value: String): String? =
        format?.validate(value)?.orElse(null)

fun validatePattern(pattern: Pattern?, value: String): String? =
        if (false == pattern?.matcher(value)?.matches()) "Has to match pattern $pattern"
        else null