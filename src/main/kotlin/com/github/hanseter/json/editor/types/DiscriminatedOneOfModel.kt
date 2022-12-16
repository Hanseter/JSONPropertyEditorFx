package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.schemaExtensions.synthetic
import com.github.hanseter.json.editor.util.EditorContext
import org.everit.json.schema.CombinedSchema
import org.everit.json.schema.ConstSchema
import org.everit.json.schema.ObjectSchema
import org.everit.json.schema.Schema
import org.json.JSONObject

/**
 * A variant oneOf model to provide enhanced support for discriminated unions.
 */
class DiscriminatedOneOfModel(
    schema: EffectiveSchema<CombinedSchema>,
    editorContext: EditorContext,
    val discriminatorKey: String
) : OneOfModel(schema, editorContext) {

    override fun tryGuessActualSchema(value: Any?): Pair<Int, Schema?> {
        val data = value as? JSONObject ?: return -1 to null
        val discriminatorValue = data.opt(discriminatorKey) ?: return -1 to null

        val found = schema.baseSchema.subschemas.find {
            val discSchema = getDiscriminatorSchema(it) ?: return@find false
            discSchema.permittedValue == discriminatorValue
        }
        val index = schema.baseSchema.subschemas.indexOf(found)

        return index to found
    }

    override fun produceValueForNewType(
        schema: ObjectSchema,
        keysToRemove: Set<String>,
        keysToKeep: Set<String>
    ): JSONObject? {
        return super.produceValueForNewType(schema, keysToRemove, keysToKeep)?.apply {
            val discriminatorValue = getDiscriminatorSchema(schema)?.permittedValue

            put(discriminatorKey, discriminatorValue)
        }
    }

    override fun isValid(schema: Schema, data: Any): Boolean {
        val descSchema = getDiscriminatorSchema(schema) ?: return false

        val descValue = (data as? JSONObject)?.opt(discriminatorKey) ?: return false

        return descSchema.permittedValue == descValue
    }

    private fun getDiscriminatorSchema(schema: Schema): ConstSchema? {
        return Companion.getDiscriminatorSchema(schema, discriminatorKey)
    }

    companion object {
        fun getDiscriminatorSchema(schema: Schema, key: String): ConstSchema? {
            val objectSchema = schema as? ObjectSchema ?: return null

            if (key !in objectSchema.requiredProperties) {
                return null
            }

            val subSchema = objectSchema.propertySchemas[key]

            return when {
                subSchema is ConstSchema -> subSchema
                subSchema is CombinedSchema && subSchema.criterion == CombinedSchema.ALL_CRITERION && subSchema.synthetic -> {
                    subSchema.subschemas.firstNotNullOf {
                        it as? ConstSchema
                    }
                }

                else -> null
            }

        }
    }


}