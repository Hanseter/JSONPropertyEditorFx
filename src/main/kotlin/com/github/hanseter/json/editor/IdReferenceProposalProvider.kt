package com.github.hanseter.json.editor

import org.everit.json.schema.StringSchema
import org.json.JSONObject

interface IdReferenceProposalProvider {
    object IdReferenceProposalProviderEmpty : IdReferenceProposalProvider {
        override fun calcCompletionProposals(part: String?, editedElement: String, editedSchema: StringSchema): List<String> = emptyList()
        override fun getReferenceDescription(reference: String?, editedElement: String, editedSchema: StringSchema): String = ""
        override fun isValidReference(userInput: String?, editedElement: String, editedSchema: StringSchema): Boolean = true
    }

    /**
     * Provides a list of valid proposals for what has already been typed.
     *
     * @param part The input so far.
     * @return The calculated proposals.
     */
    fun calcCompletionProposals(part: String?, editedElement: String, editedSchema: StringSchema): List<String>

    /**
     * Returns a descriptive string for the element referenced by the provided reference.
     * If there is no element referenced by the reference or there is no description an empty string shall be returned.
     * @param reference The typed reference id
     * @return A desciptive string for the element identified by the preference.
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

    /**
     * If this is true the user will be offered the possibility to open this elements.
     * What opening means is up to your application, however it is implied to the user that he will be able to edit this elements after request.
     * @return Whether the user shall be given the option to "open" this element
     */
    fun isOpenable(id: String): Boolean = false

    /**
     * Opens this element, this means that the reuqested element should be in some way editable after this call.
     * If this means the element will also be displayed in this editor on in another is up to your application.
     */
    fun openElement(id: String) {}

    data class DataWithSchema(val data: JSONObject, val schema: JSONObject)
}