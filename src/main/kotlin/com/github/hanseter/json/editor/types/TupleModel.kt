package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.SchemaNormalizer
import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import com.github.hanseter.json.editor.util.BindableJsonType
import org.everit.json.schema.ArraySchema
import org.everit.json.schema.Schema
import org.json.JSONArray

class TupleModel(override val schema: EffectiveSchema<ArraySchema>, val contentSchemas: List<Schema>) : TypeModel<JSONArray?, SupportedType.ComplexType.TupleType> {
    override val supportedType: SupportedType.ComplexType.TupleType
        get() = SupportedType.ComplexType.TupleType
    override var bound: BindableJsonType? = null
    override val defaultValue: JSONArray?
        get() = (schema.defaultValue as? JSONArray)?.let { SchemaNormalizer.deepCopy(it)}

    override var value: JSONArray?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
        }
    override val previewString: PreviewString
        get() = PreviewString(JsonPropertiesMl.bundle.getString("jsonEditor.controls.tuple.preview"),isPseudoValue = true)

    companion object {
        val CONVERTER: (Any?) -> JSONArray? = { it as? JSONArray }
    }
}