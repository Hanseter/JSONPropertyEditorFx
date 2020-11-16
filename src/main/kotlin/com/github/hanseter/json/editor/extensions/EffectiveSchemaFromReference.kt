package com.github.hanseter.json.editor.extensions

import org.everit.json.schema.ReferenceSchema
import org.everit.json.schema.Schema

class EffectiveSchemaFromReference<T : Schema>(override val parent: EffectiveSchema<ReferenceSchema>, override val baseSchema: T, customTitle: String? = null) : EffectiveSchema<T> {
    override val title = customTitle ?: parent.title ?: baseSchema.title ?: getPropertyName()

    override val pointer: List<String>
        get() = parent.pointer

    override fun getPropertyName(): String = parent.getPropertyName()
}