package com.github.hanseter.json.editor

import java.net.URI

interface ResolutionScopeProvider {
    /**
     * Empty instance which provides an empty URI.
     */
    object ResolutionScopeProviderEmpty : ResolutionScopeProvider {
        override fun getResolutionScopeForElement(elementId: String): URI? = null
    }

    /**
     * Provides a resolution scope URI for a requested element ID.
     * @param elementId The ID of the element
     * @return The URI of the resolution scope.
     */
    fun getResolutionScopeForElement(elementId: String) : URI?
}
