package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.util.*
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

    val viewOptions: ViewOptions
        get() = ViewOptions(
            idRefDisplayMode = context.idRefDisplayMode,
            decimalFormatSymbols = context.decimalFormatSymbols,
            groupBy = PropertyGrouping.NONE
        )

    companion object {
        val CONVERTER: (Any) -> String = { it as? String ?: it.toString() }
        private fun idToString(
            value: String?,
            context: EditorContext,
            schema: EffectiveSchema<StringSchema>
        ): String? {
            if (value == null) return null
            val desc = context.refProvider.get()
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