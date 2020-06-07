package com.github.hanseter.json.editor.util

import org.json.JSONObject
import org.everit.json.schema.Schema
import com.github.hanseter.json.editor.extensions.SchemaWrapper

class BindableJsonObject(parent: BindableJsonType?, val obj: JSONObject) : BindableJsonType(parent) {
	override protected fun setValueInternal(schema: SchemaWrapper<*>, value: Any?) {
		val key = schema.getPropertyName()
		obj.put(key, value)
	}

	override fun getValue(schema: SchemaWrapper<*>): Any? = obj.opt(schema.getPropertyName())
}