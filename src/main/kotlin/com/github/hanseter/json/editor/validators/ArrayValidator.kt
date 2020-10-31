package com.github.hanseter.json.editor.validators

import org.everit.json.schema.ArraySchema
import org.json.JSONArray
import org.json.JSONObject

class ArrayValidator(schema: ArraySchema) {
}

private fun createArrayValidators(schema: ArraySchema): List<Validator<JSONArray?>> {
    val validators = mutableListOf<Validator<JSONArray?>>()
    if (schema.needsUniqueItems()) {
        validators.add(Validator { validateChildUniqueness(it) })
    }
    createChildCountValidator(schema)?.also { validators.add(it) }
    return validators
}

private fun validateChildUniqueness(value: JSONArray?): List<String> {
    if (value == null) return emptyList()

    for (i in 0 until value.length()) {
        for (j in i + 1 until value.length()) {
            if (areSame(value.get(i), value.get(j))) {
                return listOf("Items $i and $j are identical")
            }
        }
    }
    return emptyList()
}

private fun areSame(a: Any?, b: Any?) = when (a) {
    is JSONObject -> a.similar(b)
    is JSONArray -> a.similar(b)
    else -> a == b
}

private fun createChildCountValidator(schema: ArraySchema): Validator<JSONArray?>? =
        when {
            schema.maxItems != null && schema.minItems != null -> Validator { value ->
                when {
                    value == null -> emptyList()
                    value.length() > schema.maxItems || value.length() < schema.minItems -> listOf("Has to have between ${schema.minItems} and ${schema.maxItems} items")
                    else -> emptyList()
                }
            }
            schema.maxItems != null -> Validator { value ->
                when {
                    value == null -> emptyList()
                    value.length() > schema.maxItems -> listOf("Has to have at most ${schema.maxItems} items")
                    else -> emptyList()
                }
            }
            schema.minItems != null -> Validator { value ->
                when {
                    value == null -> emptyList()
                    value.length() > schema.minItems -> listOf("Has to have at least ${schema.minItems} items")
                    else -> emptyList()
                }
            }
            else -> null
        }