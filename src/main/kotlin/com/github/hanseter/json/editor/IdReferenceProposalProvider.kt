package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.util.IdRefDisplayMode
import org.everit.json.schema.StringSchema
import org.json.JSONObject
import java.util.stream.Stream

interface IdReferenceProposalProvider {
    object IdReferenceProposalProviderEmpty : IdReferenceProposalProvider {
        override fun calcCompletionProposals(part: String?, editedElement: String, editedSchema: StringSchema, idRefMode: IdRefDisplayMode): Stream<String> = Stream.empty()
        override fun getReferenceDescription(reference: String?, editedElement: String, editedSchema: StringSchema): String = ""
        override fun isValidReference(userInput: String?, editedElement: String, editedSchema: StringSchema): Boolean = true
    }

    /**
     * Provides a list of valid proposals for what has already been typed.
     *
     * @param part The input so far.
     * @return The calculated proposals.
     */
    fun calcCompletionProposals(part: String?, editedElement: String, editedSchema: StringSchema, idRefMode: IdRefDisplayMode): Stream<String>

    /**
     * Returns a descriptive string for the element referenced by the provided reference.
     * If there is no element referenced by the reference or there is no description an empty string shall be returned.
     * @param reference The typed reference id
     * @return A descriptive string for the element identified by the preference.
     */
    fun getReferenceDescription(reference: String?, editedElement: String, editedSchema: StringSchema): String

    /**
     * Checks whether the provided user input is a valid reference.
     *
     * @param userInput The users input.
     * @return Whether the provided user input is a valid reference.
     */
    fun isValidReference(userInput: String?, editedElement: String, editedSchema: StringSchema): Boolean

    /**
     * Provides the data and schema for the object identified by the provided id. This data will be used for a preview.
     * If no preview should be displayed or no data for the requested id is available
     * @return Data and schema for the element.
     */
    fun getDataAndSchema(id: String): DataWithSchema? = null

    data class DataWithSchema(val data: JSONObject, val schema: JSONObject)
}