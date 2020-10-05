package com.github.hanseter.json.editor.util

import com.github.hanseter.json.editor.extensions.SchemaWrapper

class BindableJsonArrayEntry(private val parentArr: BindableJsonArray, private val index: Int) :
	BindableJsonType(parentArr) {

	override fun setValueInternal(schema: SchemaWrapper<*>, value: Any?) =
		parentArr.setValueInternal(index, value)

	override fun getValue(schema: SchemaWrapper<*>): Any? =
		parentArr.getValue(index)
}