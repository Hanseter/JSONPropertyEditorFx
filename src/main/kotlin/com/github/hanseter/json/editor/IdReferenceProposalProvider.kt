package com.github.hanseter.json.editor

import org.json.JSONObject

interface IdReferenceProposalProvider {
	object IdReferenceProposalProviderEmpty : IdReferenceProposalProvider {
		override fun calcCompletionProposals(part: String): List<String> = emptyList()
		override fun getReferenceDesciption(reference: String): String = ""
		override fun isValidReference(userInput: String?): Boolean = true
	}

	/**
	 * Provides a list of valid proposals for what has already been typed.
	 *
	 * @param part The input so far.
	 * @return The calculated proposals.
	 */
	fun calcCompletionProposals(part: String): List<String>

	/**
	 * Returns a descriptive string for the element referenced by the provided reference.
	 * If there is no element referenced by the reference or there is no description an empty string shall be returned.
	 * @param reference The typed reference id
	 * @return A desciptive string for the element identified by the preference.
	 */
	fun getReferenceDesciption(reference: String): String

	/**
	 * Checks whether the provided user input is a valid reference.
	 *
	 * @param userInput The users input.
	 * @return Whether the provided user input is a valid reference.
	 */
	fun isValidReference(userInput: String?): Boolean

	/**
	 * Provides the data and schema for the object identified by the provided id. This data will be used for a preview.
	 * If no preview should be displayed or no data for the requested id is available
	 * @return Data and schema for the element.
	 */
	fun getDataAndSchema(id: String): Pair<JSONObject, JSONObject>? = null
}