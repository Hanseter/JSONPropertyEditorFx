package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.SchemaNormalizer
import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import com.github.hanseter.json.editor.util.BindableJsonType
import org.everit.json.schema.ArraySchema
import org.everit.json.schema.EnumSchema
import org.everit.json.schema.Schema
import org.json.JSONArray

sealed class ArrayModelBase<S : Schema, T : SupportedType<JSONArray?>>(
    override val schema: EffectiveSchema<ArraySchema>,
    val contentSchema: S,
    override val supportedType: T
) : TypeModel<JSONArray?, T> {
    override var bound: BindableJsonType? = null
    override val defaultValue: JSONArray?
        get() = (schema.defaultValue as? JSONArray)?.let { SchemaNormalizer.deepCopy(it) }

    override var value: JSONArray?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
        }
    override val previewString: PreviewString
        get() = PreviewString.createPseudo(
            arrayToElementString(value),
            arrayToElementString(defaultValue),
            rawValue,
        )

    companion object {
        val CONVERTER: (Any?) -> JSONArray? = { it as? JSONArray }
        private fun arrayToElementString(value: JSONArray?): String? {
            if (value == null) return null
            return if (value.length() == 1) {
                JsonPropertiesMl.bundle.getString("jsonEditor.control.array.element").format(1)
            } else {
                JsonPropertiesMl.bundle.getString("jsonEditor.control.array.elements")
                    .format(value.length())
            }
        }
    }
}

class ArrayModel(schema: EffectiveSchema<ArraySchema>, contentSchema: Schema) :
    ArrayModelBase<Schema, SupportedType.ComplexType.ArrayType>(
        schema,
        contentSchema,
        SupportedType.ComplexType.ArrayType
    )

class EnumSetModel(schema: EffectiveSchema<ArraySchema>, contentSchema: EnumSchema) :
    ArrayModelBase<EnumSchema, SupportedType.SimpleType.EnumSetType>(
        schema,
        contentSchema,
        SupportedType.SimpleType.EnumSetType
    )