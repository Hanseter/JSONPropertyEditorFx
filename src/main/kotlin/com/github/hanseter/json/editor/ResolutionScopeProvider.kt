package com.github.hanseter.json.editor

import java.net.URI

fun interface ResolutionScopeProvider {
    /**
     * Empty instance which provides an empty URI.
     */
    object ResolutionScopeProviderEmpty : ResolutionScopeProvider {
        override fun getResolutionScopeForElement(objId: String): URI? = null
    }

    /**
     * Provides a resolution scope URI for a requested element ID.
     * @param objId The ID of the element
     * @return The URI of the resolution scope.
     */
    fun getResolutionScopeForElement(objId: String) : URI?
}
