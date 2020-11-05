//package com.github.hanseter.json.editor.validators
//
//import com.github.hanseter.json.editor.IdReferenceProposalProvider
//import com.github.hanseter.json.editor.actions.ActionTargetSelector
//import com.github.hanseter.json.editor.types.SupportedType
//import com.github.hanseter.json.editor.types.TypeModel
//import org.everit.json.schema.StringSchema
//
//class IdReferenceValidator(private val referenceProposalProvider: IdReferenceProposalProvider) : Validator {
//    override val selector: ActionTargetSelector = ActionTargetSelector { it.supportedType == SupportedType.SimpleType.IdReferenceType }
//
//    override fun validate(model: TypeModel<*, *>): List<String> =
//            validateString(model.schema.schema as StringSchema, model.value as? String) +
//                    (validateReference(referenceProposalProvider, model.value as? String)?.let { listOf(it) }
//                            ?: emptyList())
//
//}
//
//
//fun validateReference(referenceProposalProvider: IdReferenceProposalProvider, value: String?): String? =
//        if (referenceProposalProvider.isValidReference(value)) null
//        else "Has to be a valid reference"