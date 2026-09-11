package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import com.github.hanseter.json.editor.util.IdRefDisplayMode
import org.everit.json.schema.StringSchema

class IdReferenceModel(
    override val schema: EffectiveSchema<StringSchema>,
    private val context: EditorContext,
) : TypeModel<String?, SupportedType.SimpleType.IdReferenceType> {
    override val supportedType: SupportedType.SimpleType.IdReferenceType
        get() = SupportedType.SimpleType.IdReferenceType
    override var bound: BindableJsonType? = null
    override val defaultValue: String?
        get() = context.customizationObject().getNullIdReferencePreview(this)

    companion object {
        private fun idToString(
            value: String?,
            context: EditorContext,
            schema: EffectiveSchema<StringSchema>
        ): String? {
            if (value == null) return null
            val desc = context.refProvider()
                .getReferenceDescription(value, context.editorObjId, schema.baseSchema)
            val separator = " - "
            return when (context.idRefDisplayMode) {
                IdRefDisplayMode.DESCRIPTION_ONLY -> desc
                IdRefDisplayMode.ID_ONLY -> value
                IdRefDisplayMode.ID_WITH_DESCRIPTION -> listOf(value, desc)
                    .filter { it.isNotBlank() }
                    .joinToString(separator)

                IdRefDisplayMode.DESCRIPTION_WITH_ID -> listOf(desc, value)
                    .filter { it.isNotBlank() }
                    .joinToString(separator)
            }

        }
    }

    override val previewString: PreviewString
        get() = PreviewString.create(
            idToString(value, context, schema),
            idToString(defaultValue, context, schema),
            rawValue
        )

}