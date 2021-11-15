package com.github.hanseter.json.editor.util

import com.github.hanseter.json.editor.extensions.EffectiveSchema

class BindableJsonArrayEntry(private val parentArr: BindableJsonArray, private val index: Int) :
	BindableJsonType(parentArr) {

	override fun setValueInternal(schema: EffectiveSchema<*>, value: Any?) =
		parentArr.setValueInternal(index, value)

	override fun getValue(schema: EffectiveSchema<*>): Any? =
		getValue()

	override fun getValue(): Any? =
		parentArr.getValue(index)
}