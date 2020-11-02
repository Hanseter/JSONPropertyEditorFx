package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonType
import org.everit.json.schema.BooleanSchema

class BooleanModel(override val schema: SchemaWrapper<BooleanSchema>) : TypeModel<Boolean?, SupportedType.SimpleType.BooleanType> {
    override val supportedType: SupportedType.SimpleType.BooleanType
        get() = SupportedType.SimpleType.BooleanType
    override var bound: BindableJsonType? = null
    override val defaultValue: Boolean?
        get() = schema.schema.defaultValue as? Boolean

    override var value: Boolean?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
        }

    companion object {
        val CONVERTER: (Any?) -> Boolean? = { it as? Boolean }
    }
}