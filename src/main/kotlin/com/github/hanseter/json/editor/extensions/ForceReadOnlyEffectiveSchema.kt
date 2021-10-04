package com.github.hanseter.json.editor.extensions

import org.everit.json.schema.Schema


class ForceReadOnlyEffectiveSchema<T : Schema>(
    private val actual: EffectiveSchema<T>
) : EffectiveSchema<T> by actual {

    override val readOnly: Boolean
        get() = true

}