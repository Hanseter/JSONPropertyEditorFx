package com.github.hanseter.json.editor.extensions

import org.everit.json.schema.CombinedSchema
import org.everit.json.schema.Schema

class NullableEffectiveSchema<T : Schema>(override val parent: EffectiveSchema<CombinedSchema>,
                                          override val baseSchema: T) : EffectiveSchema<T> {
    override val title: String
        get() = parent.title

    override val pointer: List<String>
        get() = parent.pointer

    override val defaultValue: Any?
        get() = parent.defaultValue

    override val description: String?
        get() = parent.description

    override fun getPropertyName(): String = parent.getPropertyName()

}