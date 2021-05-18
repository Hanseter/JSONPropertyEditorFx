package com.github.hanseter.json.editor.extensions

import com.github.hanseter.json.editor.schemaExtensions.synthetic
import org.everit.json.schema.CombinedSchema
import org.everit.json.schema.ObjectSchema
import org.everit.json.schema.Schema

/**
 * Wraps a [Schema] and provides getters for some properties.
 * These getters should be used whenever possible instead of getting these values from the base schema, as they might adapt some values.
 */
interface EffectiveSchema<T : Schema> : BaseEffectiveSchema {

    override val parent: EffectiveSchema<*>?
    override val baseSchema: T

    private tailrec fun findSchemaLocation(schema: EffectiveSchema<*>): String? {
        if (schema.baseSchema.schemaLocation != null) return schema.baseSchema.schemaLocation
        val parent = schema.parent ?: return null
        return findSchemaLocation(parent)
    }

    override val nonSyntheticAncestor: EffectiveSchema<*>?
        get() {
            val baseSchema = parent?.baseSchema

            if (baseSchema is CombinedSchema && baseSchema.synthetic) {
                return parent?.nonSyntheticAncestor
            }

            return parent
        }

    override val objectAncestor: EffectiveSchema<ObjectSchema>?
        get() {
            val baseSchema = parent?.baseSchema

            if (baseSchema is CombinedSchema) {
                return parent?.objectAncestor
            }
            if (baseSchema is ObjectSchema) {
                @Suppress("UNCHECKED_CAST")
                return parent as EffectiveSchema<ObjectSchema>
            }
            return null
        }
}
