package com.github.hanseter.json.editor.validators

import com.github.hanseter.json.editor.IdReferenceProposalProvider
import com.github.hanseter.json.editor.actions.TargetSelector
import com.github.hanseter.json.editor.types.SupportedType
import com.github.hanseter.json.editor.types.TypeModel
import org.everit.json.schema.StringSchema
import java.util.function.Supplier

class IdReferenceValidator(private val referenceProposalProvider: Supplier<IdReferenceProposalProvider>) : Validator {
    override val selector: TargetSelector = TargetSelector { it.supportedType == SupportedType.SimpleType.IdReferenceType }

    override fun validate(model: TypeModel<*, *>, objId: String): List<String> =
            if (referenceProposalProvider.get().isValidReference(model.value as? String, objId, model.schema.baseSchema as StringSchema)) emptyList()
            else listOf("Has to be a valid reference")

}
