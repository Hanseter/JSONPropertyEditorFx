package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.util.BindableJsonType
import org.everit.json.schema.EnumSchema
import org.everit.json.schema.Schema

class EnumModel(override val schema: EffectiveSchema<Schema>, val enumSchema: EnumSchema) :
    TypeModel<String?, SupportedType.SimpleType.EnumType> {
    override val supportedType: SupportedType.SimpleType.EnumType
        get() = SupportedType.SimpleType.EnumType
    override var bound: BindableJsonType? = null
    override val defaultValue: String?
        get() = schema.defaultValue as? String

    override var value: String?
        get() = bound?.let {
            BindableJsonType.convertValue(
                it.getValue(schema),
                schema,
                StringModel.CONVERTER
            )
        }
        set(value) {
            bound?.setValue(schema, value)
        }
    override val previewString: PreviewString
        get() = PreviewString.create(
            formatEnumWithDescription(value),
            formatEnumWithDescription(defaultValue),
            rawValue
        )
    val enumDescriptions: Map<String, String?>
        get() = getEnumDescriptions(schema, enumSchema)

    private fun formatEnumWithDescription(value: String?): String? {
        if (value == null) return null

        val desc = enumDescriptions[value]
        return if (desc != null) {
            "$value - $desc"
        } else {
            value
        }
    }

    companion object {
        fun getEnumDescriptions(
            schema: EffectiveSchema<out Schema>,
            enumSchema: EnumSchema
        ): Map<String, String?> {
            val descList = ((
                    enumSchema.unprocessedProperties["enumDescriptions"]
                        ?: schema.baseSchema.unprocessedProperties["enumDescriptions"]) as? List<*>
                    )?.filterIsInstance<String>()

            return enumSchema.possibleValuesAsList
                .filterIsInstance<String>()
                .withIndex()
                .associateBy({ it.value }) {
                    descList?.getOrNull(it.index)
                }
        }
    }


}