package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.SchemaNormalizer
import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import com.github.hanseter.json.editor.util.BindableJsonType
import org.everit.json.schema.ObjectSchema
import org.json.JSONObject

class PlainObjectModel(override val schema: EffectiveSchema<ObjectSchema>) : TypeModel<JSONObject?, SupportedType.ComplexType.ObjectType> {
    override val supportedType: SupportedType.ComplexType.ObjectType
        get() = SupportedType.ComplexType.ObjectType
    override var bound: BindableJsonType? = null
    override val defaultValue: JSONObject?
        get() = (schema.defaultValue as? JSONObject)?.let {SchemaNormalizer.deepCopy(it)}

    override var value: JSONObject?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
        }
    override val previewString: PreviewString
        get() = PreviewString(JsonPropertiesMl.bundle.getString("jsonEditor.controls.object.preview"), isPseudoValue = true)

    companion object {
        val CONVERTER: (Any?) -> JSONObject? = { it as? JSONObject }
    }
}