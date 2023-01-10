package com.github.hanseter.json.editor.extensions

import org.everit.json.schema.CombinedSchema
import org.everit.json.schema.Schema

/**
 * This wrapper is used to wrap schemas that come from a combined schema (e.g. allOf[]).
 * These schemas do not need to be included when building a json pointer.
 */
class EffectiveSchemaOfCombination<T : Schema>(override val parent: EffectiveSchema<CombinedSchema>, override val baseSchema: T) : EffectiveSchema<T> {
    override val title: String
        get() = SimpleEffectiveSchema.calcSchemaTitle(baseSchema)

    override val pointer: List<String>
        get() = parent.pointer

    override val propertyName: String
        get() = parent.propertyName

    override val nonSyntheticAncestor: EffectiveSchema<*>?
        get() = parent.nonSyntheticAncestor
}