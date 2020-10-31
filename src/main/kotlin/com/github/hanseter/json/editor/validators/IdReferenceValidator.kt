package com.github.hanseter.json.editor.validators

import com.github.hanseter.json.editor.IdReferenceProposalProvider
import org.controlsfx.validation.ValidationResult
import org.everit.json.schema.StringSchema

class IdReferenceValidator(schema: StringSchema, referenceProposalProvider: IdReferenceProposalProvider) {
    val validators = listOf(StringValidator(schema), createReferenceValidation(referenceProposalProvider))

    fun validate(value: String?): List<String> = validators.flatMap { it.validate(value) }
}

fun createReferenceValidation(referenceProposalProvider: IdReferenceProposalProvider): Validator<String?> =
        Validator { value ->
            if (referenceProposalProvider.isValidReference(value)) listOf()
            else listOf("Has to be a valid reference")
        }