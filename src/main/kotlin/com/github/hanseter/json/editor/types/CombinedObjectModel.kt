package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.util.BindableJsonType
import org.everit.json.schema.CombinedSchema
import org.json.JSONObject

class CombinedObjectModel(override val schema: EffectiveSchema<CombinedSchema>) :
    TypeModel<JSONObject?, SupportedType.ComplexType.ObjectType> {
    override val supportedType: SupportedType.ComplexType.ObjectType
        get() = SupportedType.ComplexType.ObjectType
    override var bound: BindableJsonType? = null
    override val defaultValue: JSONObject?
        get() = schema.defaultValue as? JSONObject

    override var value: JSONObject?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
        }
    override val previewString: PreviewString
        get() = PreviewString.createPseudo(
            PlainObjectModel.jsonToPropertyString(value),
            PlainObjectModel.jsonToPropertyString(defaultValue),
            rawValue
        )

    companion object {
        val CONVERTER: (Any?) -> JSONObject? = { it as? JSONObject }
    }
}