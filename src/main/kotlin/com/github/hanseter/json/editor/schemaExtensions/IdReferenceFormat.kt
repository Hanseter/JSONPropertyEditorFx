package com.github.hanseter.json.editor.schemaExtensions

import com.github.hanseter.json.editor.IdReferenceProposalProvider
import java.util.*

object IdReferenceFormat {
    const val formatName = "id-reference"

    class Validator(private val idReferenceProposalProvider: IdReferenceProposalProvider) : org.everit.json.schema.FormatValidator {

        override fun formatName() = formatName

        override fun validate(subject: String?): Optional<String> =
                Optional.empty()
//				if (idReferenceProposalProvider.isValidReference(subject)) Optional.empty()
//				else Optional.of("Has to be a valid reference")
    }
}