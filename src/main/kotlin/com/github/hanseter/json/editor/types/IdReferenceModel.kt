package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.validators.StringValidator
import org.everit.json.schema.StringSchema

class IdReferenceModel(override val schema: SchemaWrapper<StringSchema>) : TypeModel<String?> {
    val validator = StringValidator(schema.schema)
    override var bound: BindableJsonType? = null
        set(value) {
            field = value
            validate()
        }
    override val defaultValue: String?
        get() = schema.schema.defaultValue as? String

    override var value: String?
        get() = bound?.let { BindableJsonType.convertValue(it.getValue(schema), schema, CONVERTER) }
        set(value) {
            bound?.setValue(schema, value)
            validate()
        }

    override var validationErrors: List<String> = emptyList()

    init {
        validate()
    }

    private fun validate() {
        validationErrors = validator.validate(value ?: defaultValue)
    }

    companion object {
        val CONVERTER: (Any) -> String = { it as? String ?: it.toString() }
    }
}