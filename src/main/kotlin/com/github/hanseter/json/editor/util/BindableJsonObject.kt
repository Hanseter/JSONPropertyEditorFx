package com.github.hanseter.json.editor.util

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import org.json.JSONObject

class BindableJsonObject(parent: BindableJsonType?, val obj: JSONObject) : BindableJsonType(parent) {
	override fun setValueInternal(schema: SchemaWrapper<*>, value: Any?) {
		val key = schema.getPropertyName()
		obj.put(key, value)
	}

	override fun getValue(schema: SchemaWrapper<*>): Any? = obj.opt(schema.getPropertyName())
}