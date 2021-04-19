package com.github.hanseter.json.editor.extensions

import org.everit.json.schema.CombinedSchema
import org.everit.json.schema.Schema

class NullableEffectiveSchema<T : Schema>(override val parent: EffectiveSchema<CombinedSchema>,
                                          override val baseSchema: T) : EffectiveSchema<T>, BaseEffectiveSchema by parent {

    override val nonSyntheticAncestor: EffectiveSchema<*>?
        get() = super.nonSyntheticAncestor
}