package com.github.hanseter.json.editor.util

import com.github.hanseter.json.editor.SchemaNormalizer
import com.github.hanseter.json.editor.extensions.EffectiveSchema
import org.json.JSONArray
import org.json.JSONObject

abstract class BindableJsonType(private val parent: BindableJsonType?) {
    private var listeners = listOf<() -> Unit>()

    fun setValue(schema: EffectiveSchema<*>, value: Any?) {
        if (jsonAwareEquals(getValue(schema), value)) return
        updateAfterChange(schema, value)
    }

    open fun updateFromChild(child: BindableJsonType, schema: EffectiveSchema<*>) {
        updateAfterChange(schema.nonSyntheticAncestor!!, child.getValue())
    }

    private fun updateAfterChange(
        schema: EffectiveSchema<*>,
        value: Any?,
    ) {
        setValueInternal(schema, value)
        if (parent != null) {
            parent.updateFromChild(this, schema)
        } else {
            listeners.forEach { it() }
        }
    }

    protected abstract fun setValueInternal(schema: EffectiveSchema<*>, value: Any?)

    abstract fun getValue(schema: EffectiveSchema<*>): Any?

    abstract fun getValue(): Any?

    fun registerListener(listener: () -> Unit) {
        this.listeners += listener
    }

    val rootType: RootBindableType
        get() = getRootType(this)

    companion object {

        private fun jsonAwareEquals(a: Any?, b: Any?): Boolean {
            return when {
                a == null -> b == null
                b == null -> false
                a is JSONObject -> a.similar(b)
                a is JSONArray -> a.similar(b)
                else -> a == b
            }
        }

        private tailrec fun getRootType(ref: BindableJsonType): RootBindableType {
            return if (ref.parent == null) ref as RootBindableType
            else getRootType(ref.parent)
        }

        fun <T> convertValue(value: Any?, schema: EffectiveSchema<*>, converter: (Any) -> T?): T? =
            when (value) {
                null -> schema.baseSchema.defaultValue?.let { SchemaNormalizer.deepCopy(converter(it)) }
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