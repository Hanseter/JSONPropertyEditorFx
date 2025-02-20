package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import org.json.JSONObject

class PreviewString private constructor(
    val string: String,
    val isDefaultValue: Boolean = false,
    val isPseudoValue: Boolean = false,
) {
    companion object {
        val NO_VALUE: PreviewString = PreviewString(JsonPropertiesMl.bundle.getString("jsonEditor.missing"), isPseudoValue = true)
        val NULL_VALUE: PreviewString = PreviewString(JsonPropertiesMl.bundle.getString("jsonEditor.nullValue"))

        fun create(value: String?, defaultValue: String?, rawValue: Any?) : PreviewString = when {
            isDefaultValue(value, defaultValue, rawValue) ->PreviewString(defaultValue!!, isDefaultValue = true)
            value != null -> PreviewString(value)
            rawValue==JSONObject.NULL->NULL_VALUE
            defaultValue != null -> PreviewString(defaultValue, isDefaultValue = true)
            else -> NO_VALUE
        }

        private fun isDefaultValue(
            value: String?,
            defaultValue: String?,
            rawValue: Any?
        ) = value != null && defaultValue != null && value == defaultValue && rawValue == null

        fun createPseudo(value:String?,defaultValue:String?,rawValue: Any?) : PreviewString = when {
            value != null -> PreviewString(value, isPseudoValue = true)
            defaultValue != null -> PreviewString(defaultValue, isDefaultValue = true, isPseudoValue = true)
            rawValue==JSONObject.NULL->NULL_VALUE
            else -> NO_VALUE
        }

        fun createPseudo(value: String) : PreviewString = PreviewString(value, isPseudoValue = true)

    }
}