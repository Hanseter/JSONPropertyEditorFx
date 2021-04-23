package com.github.hanseter.json.editor.extensions

import org.everit.json.schema.CombinedSchema
import org.everit.json.schema.NullSchema
import org.everit.json.schema.Schema

/**
 * Effective schema that exposes only part of the schema for UI building.
 * Used when the schema contains validation-only keywords like "not" or "if", or when it's nullable.
 */
class PartialEffectiveSchema<T : Schema>(
        override val parent: EffectiveSchema<CombinedSchema>,
        override val baseSchema: T,
) : EffectiveSchema<T>, BaseEffectiveSchema by parent {

    override val schemaForValidation: Schema
        get() = parent.schemaForValidation

    override val nonSyntheticAncestor: EffectiveSchema<*>?
        get() = super.nonSyntheticAncestor

    val allowsNull: Boolean
        get() = parent.baseSchema.subschemas.any { allowsNull(it) }

    private fun allowsNull(schema: Schema): Boolean {
        return schema is NullSchema
                || schema is CombinedSchema
                && schema.criterion == CombinedSchema.ANY_CRITERION
                && schema.subschemas.any { allowsNull(it) }
    }
}