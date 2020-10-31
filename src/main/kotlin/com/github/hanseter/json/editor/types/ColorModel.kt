package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonType
import org.everit.json.schema.StringSchema

class ColorModel(override val schema: SchemaWrapper<StringSchema>) : TypeModel<String?> {
    override var bound: BindableJsonType? = null
    override val defaultValue: String?
        get() = schema.schema.defaultValue as? String

    override var value: String?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, StringModel.CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
        }

    override val validationErrors: List<String> = emptyList()
}