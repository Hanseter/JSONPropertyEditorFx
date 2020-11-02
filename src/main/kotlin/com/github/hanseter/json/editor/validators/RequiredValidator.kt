package com.github.hanseter.json.editor.validators

import com.github.hanseter.json.editor.actions.ActionTargetSelector
import com.github.hanseter.json.editor.types.TypeModel
import org.everit.json.schema.ObjectSchema

object RequiredValidator : Validator {
    override val selector: ActionTargetSelector = ActionTargetSelector {
        true == (it.schema.parent?.schema as? ObjectSchema)?.requiredProperties?.contains(it.schema.getPropertyName())
    }

    override fun validate(model: TypeModel<*, *>): List<String> =
            if (model.rawValue == null) listOf("Must not be null")
            else emptyList()
}