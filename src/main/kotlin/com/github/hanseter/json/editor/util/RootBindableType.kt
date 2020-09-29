package com.github.hanseter.json.editor.util

import com.github.hanseter.json.editor.extensions.SchemaWrapper

class RootBindableType<T>(var value: T?): BindableJsonType(null) {
    override fun setValueInternal(schema: SchemaWrapper<*>, value: Any?) {
        this.value = value as T
    }

    override fun getValue(schema: SchemaWrapper<*>): Any? =value
}