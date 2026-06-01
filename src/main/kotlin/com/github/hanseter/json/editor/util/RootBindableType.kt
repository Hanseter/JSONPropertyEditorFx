package com.github.hanseter.json.editor.util

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import org.json.JSONObject

class RootBindableType(val elemId: String, private var value: JSONObject) : BindableJsonType(null) {
    override fun setValueInternal(schema: EffectiveSchema<*>, value: Any?) {
        this.value = value as JSONObject
    }

    override fun getValue(schema: EffectiveSchema<*>): JSONObject = value

    override fun getValue(): JSONObject = value
}