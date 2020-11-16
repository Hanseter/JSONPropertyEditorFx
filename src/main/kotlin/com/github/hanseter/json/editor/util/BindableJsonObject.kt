package com.github.hanseter.json.editor.util

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import org.json.JSONObject

class BindableJsonObject(parent: BindableJsonType?, val obj: JSONObject) : BindableJsonType(parent) {
	override fun setValueInternal(schema: EffectiveSchema<*>, value: Any?) {
		val key = schema.getPropertyName()
		obj.put(key, value)
	}

	override fun getValue(schema: EffectiveSchema<*>): Any? = obj.opt(schema.getPropertyName())
}