package com.github.hanseter.json.editor.extensions

import org.everit.json.schema.CombinedSchema
import org.everit.json.schema.Schema

/**
 * Effective schema that exposes only part of the schema for UI building.
 */
class PartialEffectiveSchema<T : Schema>(
        override val parent: EffectiveSchema<CombinedSchema>,
        override val baseSchema: T,
) : EffectiveSchema<T>, BaseEffectiveSchema by parent {

    override val schemaForValidation: Schema
        get() = parent.schemaForValidation

    override val nonSyntheticAncestor: EffectiveSchema<*>?
        get() = super.nonSyntheticAncestor

}