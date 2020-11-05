package com.github.hanseter.json.editor.validators

import com.github.hanseter.json.editor.actions.TargetSelector
import com.github.hanseter.json.editor.types.TypeModel
import org.everit.json.schema.ObjectSchema

object RequiredValidator : Validator {
    override val selector: TargetSelector = TargetSelector {
        true == (it.schema.parent?.schema as? ObjectSchema)?.requiredProperties?.contains(it.schema.getPropertyName())
    }

    override fun validate(model: TypeModel<*, *>): List<String> =
            if (model.value == null) listOf("Must not be null")
            else emptyList()
}