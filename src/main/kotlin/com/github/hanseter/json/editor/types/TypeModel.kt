package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonType

interface TypeModel<T> {
    val schema: SchemaWrapper<*>
    var bound: BindableJsonType?
    val defaultValue: T
    var value: T
    val validationErrors: List<String>

    val rawValue: Any?
        get() = bound?.getValue(schema)
}