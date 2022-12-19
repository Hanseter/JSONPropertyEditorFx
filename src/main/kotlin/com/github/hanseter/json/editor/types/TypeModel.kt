package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.util.BindableJsonType

interface TypeModel<T, R: SupportedType<T>> {
    val supportedType: R
    val schema: EffectiveSchema<*>
    var bound: BindableJsonType?
    val defaultValue: T
    var value: T

    val previewString: PreviewString

    val rawValue: Any?
        get() = bound?.getValue(schema)
}