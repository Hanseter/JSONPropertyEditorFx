package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.util.BindableJsonType
import org.everit.json.schema.EnumSchema
import org.everit.json.schema.Schema

class EnumModel(override val schema: EffectiveSchema<Schema>, val enumSchema: EnumSchema) : TypeModel<String?, SupportedType.SimpleType.EnumType> {
    override val supportedType: SupportedType.SimpleType.EnumType
        get() = SupportedType.SimpleType.EnumType
    override var bound: BindableJsonType? = null
    override val defaultValue: String?
        get() = schema.defaultValue as? String

    override var value: String?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, StringModel.CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
        }
}