package com.github.hanseter.json.editor.util

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import org.json.JSONObject

abstract class BindableJsonType(private val parent: BindableJsonType?) {
    private var listeners = listOf<() -> Unit>()

    fun setValue(schema: EffectiveSchema<*>, value: Any?) {
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

    protected abstract fun setValueInternal(schema: EffectiveSchema<*>, value: Any?)

    abstract fun getValue(schema: EffectiveSchema<*>): Any?


    fun registerListener(listener: () -> Unit) {
        this.listeners += listener
    }

    companion object {
        fun <T> convertValue(value: Any?, schema: EffectiveSchema<*>, converter: (Any) -> T?): T? =
                when (value) {
                    null -> schema.baseSchema.defaultValue?.let(converter)
                    JSONObject.NULL -> null
                    else -> {
                        val converted = converter(value)
                        if (converted == null) {
                            //TODO This should not happen with correct data and should be logged
                        }
                        converted
                    }
                }
    }
}