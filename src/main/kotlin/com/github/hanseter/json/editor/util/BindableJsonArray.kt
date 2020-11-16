package com.github.hanseter.json.editor.util

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import org.json.JSONArray
import org.json.JSONObject

class BindableJsonArray(parent: BindableJsonType?, private val arr: JSONArray) : BindableJsonType(parent) {

	override fun setValueInternal(schema: EffectiveSchema<*>, value: Any?) =
		setValueInternal(schema.getPropertyName().toInt(), value)

	fun setValueInternal(index: Int, value: Any?) {
		arr.put(index, value)
	}

	override fun getValue(schema: EffectiveSchema<*>): Any? =
		getValue(schema.getPropertyName().toInt())

	fun getValue(index: Int): Any? {
		val ret = arr.opt(index)
		if (ret == JSONObject.NULL) {
			return null
		}
		return ret
	}

	fun getSize(): Int = arr.length()
}