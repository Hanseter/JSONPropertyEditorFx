package com.github.hanseter.json.editor.util

import org.json.JSONObject
import org.everit.json.schema.Schema
import org.everit.json.schema.ObjectSchema
import org.everit.json.schema.ArraySchema
import org.json.JSONArray
import com.github.hanseter.json.editor.extensions.SchemaWrapper

abstract class BindableJsonType(private val parent: BindableJsonType?) {
	private var listeners = listOf<() -> Unit>()

	fun setValue(schema: SchemaWrapper<*>, value: Any) {
		setValueInternal(schema, value)
		onValueChanged()
	}

	private fun onValueChanged() {
		if (parent != null) {
			parent.onValueChanged()
		} else {
			for (listener in listeners) {
				listener()
			}
		}
	}

	protected abstract fun setValueInternal(schema: SchemaWrapper<*>, value: Any)

	abstract fun getValue(schema: SchemaWrapper<*>): Any?

	fun registerListener(listener: () -> Unit) {
		this.listeners += listener
	}
}