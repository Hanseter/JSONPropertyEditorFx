package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonType

interface TypeModel<T, R: SupportedType<T>> {
    val supportedType: R
    val schema: SchemaWrapper<*>
    var bound: BindableJsonType?
    val defaultValue: T
    var value: T

    val rawValue: Any?
        get() = bound?.getValue(schema)
}