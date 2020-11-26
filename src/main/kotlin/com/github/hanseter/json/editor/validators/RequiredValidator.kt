package com.github.hanseter.json.editor.validators

import com.github.hanseter.json.editor.actions.TargetSelector
import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.types.TypeModel
import org.everit.json.schema.ObjectSchema

object RequiredValidator : Validator {
    override val selector: TargetSelector = TargetSelector {
        isRequiredSchema(it.schema)
    }

    override fun validate(model: TypeModel<*, *>): List<String> =
            if (model.value == null) listOf("Must not be null")
            else emptyList()
}

fun isRequiredSchema(schema: EffectiveSchema<*>): Boolean =
        true == (schema.parent?.baseSchema as? ObjectSchema)?.requiredProperties?.contains(schema.getPropertyName())
