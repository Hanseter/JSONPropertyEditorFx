package com.github.hanseter.json.editor.validators

import com.github.hanseter.json.editor.actions.ActionTargetSelector
import com.github.hanseter.json.editor.types.SupportedType
import com.github.hanseter.json.editor.types.TypeModel
import org.everit.json.schema.ArraySchema
import org.json.JSONArray
import org.json.JSONObject

object ArrayValidator : Validator {
    override val selector: ActionTargetSelector = ActionTargetSelector { it.supportedType == SupportedType.ComplexType.ArrayType }

    override fun validate(model: TypeModel<*, *>): List<String> {
        val schema = model.schema.schema as ArraySchema
        val value = model.value as? JSONArray ?: return emptyList()
        val ret = mutableListOf<String>()
        if (schema.needsUniqueItems()) {
            validateChildUniqueness(value)?.also { ret.add(it) }
        }
        validateChildCount(schema.maxItems, schema.minItems, value)?.also { ret.add(it) }
        return ret
    }
}

private fun validateChildUniqueness(value: JSONArray): String? {
    for (i in 0 until value.length()) {
        for (j in i + 1 until value.length()) {
            if (areSame(value.get(i), value.get(j))) {
                return "Items $i and $j are identical"
            }
        }
    }
    return null
}

private fun areSame(a: Any?, b: Any?) = when (a) {
    is JSONObject -> a.similar(b)
    is JSONArray -> a.similar(b)
    else -> a == b
}

private fun validateChildCount(maxItems: Int?, minItems: Int?, value: JSONArray): String? =
        when {
            maxItems != null && minItems != null -> when {
                value.length() > maxItems || value.length() < minItems -> "Has to have between $minItems and $maxItems items"
                else -> null
            }
            maxItems != null -> when {
                value.length() > maxItems -> "Has to have at most $maxItems items"
                else -> null
            }
            minItems != null -> when {
                value.length() > minItems -> "Has to have at least $minItems items"
                else -> null
            }
            else -> null
        }