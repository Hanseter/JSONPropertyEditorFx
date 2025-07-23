package com.github.hanseter.json.editor.extensions

import org.everit.json.schema.Schema

class SimpleEffectiveSchema<T : Schema>(
    override val parent: EffectiveSchema<*>?,
    override val baseSchema: T,
    customTitle: String? = null
) : EffectiveSchema<T> {
    override val title = customTitle ?: calcSchemaTitle(baseSchema)

    override val propertyName: String = calcPropertyName(baseSchema)

    companion object {
        fun calcSchemaTitle(schema: Schema) = schema.title ?: calcPropertyName(schema)

        fun calcPropertyName(schema: Schema): String = (schema.schemaLocation
            ?: schema.location.toString()).split('/').last()
    }
}