package com.github.hanseter.json.editor.util

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import org.json.JSONObject

class BindableJsonObject(parent: BindableJsonType?, val obj: JSONObject) : BindableJsonType(parent) {
    override fun setValueInternal(schema: EffectiveSchema<*>, value: Any?) {
        obj.put(schema.propertyName, value)
    }

    override fun getValue(schema: EffectiveSchema<*>): Any? = obj.opt(schema.propertyName)

    override fun getValue(): JSONObject = obj
}