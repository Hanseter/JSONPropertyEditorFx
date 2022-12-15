package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import com.github.hanseter.json.editor.util.IdRefDisplayMode
import org.everit.json.schema.StringSchema

class IdReferenceModel(
    override val schema: EffectiveSchema<StringSchema>,
    private val context: EditorContext,
) :
    TypeModel<String?, SupportedType.SimpleType.IdReferenceType> {
    override val supportedType: SupportedType.SimpleType.IdReferenceType
        get() = SupportedType.SimpleType.IdReferenceType
    override var bound: BindableJsonType? = null
    override val defaultValue: String?
        get() = schema.defaultValue as? String

    override var value: String?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
        }

    companion object {
        val CONVERTER: (Any) -> String = { it as? String ?: it.toString() }
        private fun idToString(
            value: String?,
            context: EditorContext,
            schema: EffectiveSchema<StringSchema>
        ): String {
            if (value == null) return ""
            val desc=context.refProvider.get()
                .getReferenceDescription(value, context.editorObjId, schema.baseSchema)
            return when (context.idRefDisplayMode) {
                IdRefDisplayMode.DESCRIPTION_ONLY -> desc
                IdRefDisplayMode.ID_ONLY -> value
                IdRefDisplayMode.ID_WITH_DESCRIPTION -> "$value - $desc"
                IdRefDisplayMode.DESCRIPTION_WITH_ID -> "$desc - $value"
            }

        }
    }

    override val previewString: PreviewString
        get() = when {
            value != null -> PreviewString(idToString(value, context, schema))
            defaultValue != null -> PreviewString(
                idToString(defaultValue, context, schema),
                isDefaultValue = true
            )

            else -> PreviewString.NO_VALUE
        }


}