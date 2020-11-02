package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.validators.StringValidator
import org.everit.json.schema.StringSchema

class StringModel(override val schema: SchemaWrapper<StringSchema>) : TypeModel<String?, SupportedType.SimpleType.StringType> {
    override val supportedType: SupportedType.SimpleType.StringType
        get() = SupportedType.SimpleType.StringType
    override var bound: BindableJsonType? = null
    override val defaultValue: String?
        get() = schema.schema.defaultValue as? String

    override var value: String?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
        }

    companion object {
        val CONVERTER: (Any) -> String = { it as? String ?: it.toString() }
    }
}