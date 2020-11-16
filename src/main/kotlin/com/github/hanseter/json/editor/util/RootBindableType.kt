package com.github.hanseter.json.editor.util

import com.github.hanseter.json.editor.extensions.EffectiveSchema

class RootBindableType<T>(var value: T?): BindableJsonType(null) {
    override fun setValueInternal(schema: EffectiveSchema<*>, value: Any?) {
        this.value = value as T
    }

    override fun getValue(schema: EffectiveSchema<*>): Any? =value
}