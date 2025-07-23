package com.github.hanseter.json.editor

import org.everit.json.schema.ObjectSchema
import org.json.JSONObject
import java.net.URI

/**
 * A schema with its raw json, the normalized schema and its parsed representation.
 */
class ParsedSchema private constructor(
    val raw: JSONObject,
    val normalized: JSONObject,
    val parsed: ObjectSchema
) {

    companion object {

        /**
         * Creates a [ParsedSchema] from a raw schema.
         */
        @JvmStatic
        @JvmOverloads
        fun create(
            schema: JSONObject,
            resolutionScope: URI?,
            readOnly: Boolean = false
        ): ParsedSchema? {
            val normalized = if (resolutionScope == null) SchemaNormalizer.normalize(schema)
            else SchemaNormalizer.normalize(schema, resolutionScope)
            return create(schema, normalized, readOnly)
        }

        /**
         * Creates a [ParsedSchema] from a raw schema and an already normalized schema.
         */
        @JvmStatic
        @JvmOverloads
        fun create(
            schema: JSONObject,
            normalized: JSONObject,
            readOnly: Boolean = false
        ): ParsedSchema? {
            val parsed =
                SchemaNormalizer.parseNormalizedSchema(normalized, readOnly) as? ObjectSchema
                    ?: return null
            return ParsedSchema(schema, normalized, parsed)
        }

        /**
         * Creates a [ParsedSchema] from an already normalized ([SchemaNormalizer.normalize]) schema.
         */
        @JvmStatic
        @JvmOverloads
        fun createFromNormalized(schema: JSONObject, readOnly: Boolean = false): ParsedSchema? {
            val parsed =
                SchemaNormalizer.parseNormalizedSchema(schema, readOnly) as? ObjectSchema
                    ?: return null
            return ParsedSchema(schema, schema, parsed)
        }

    }
}