package com.github.hanseter.json.editor.extensions

import org.everit.json.schema.ArraySchema
import org.everit.json.schema.Schema

class EffectiveSchemaInArray<T : Schema>(override val parent: EffectiveSchema<ArraySchema>, override val baseSchema: T, val index: Int) : EffectiveSchema<T> {
    override val title = index.toString()

    override val propertyName: String
        get() = title

}