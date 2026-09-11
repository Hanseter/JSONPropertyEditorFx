package com.github.hanseter.json.editor.util

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.types.ArrayModel
import com.github.hanseter.json.editor.types.SupportedType
import com.github.hanseter.json.editor.types.TypeModel
import org.json.JSONArray
import org.json.JSONObject
import sun.util.calendar.CalendarUtils.mod

abstract class BindableJsonType(private val parent: BindableJsonType?) {
    private var listeners = listOf<() -> Unit>()

    fun setValue(schema: EffectiveSchema<*>, value: Any?) {
        if (jsonAwareEquals(getValue(schema), value)) return
        updateAfterChange(schema, value)
    }

    open fun updateFromChild(child: BindableJsonType, schema: EffectiveSchema<*>) {
        updateAfterChange(schema.nonSyntheticAncestor!!, child.getValue())
    }

    protected fun updateAfterChange(
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

    fun <T, R: SupportedType<T> > valueOrDefault(model: TypeModel<T, R>): T? {
        return when (val value = getValue(model.schema)?.let { model.supportedType.coerce(it) }) {
            null -> model.defaultValue
            JSONObject.NULL -> null
            else -> value
        }
    }

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

    }
}