package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.ResourceProvider.Companion.RESOURCE_EXTENSIONS_KEY
import com.github.hanseter.json.editor.extensions.EffectiveSchema
import org.everit.json.schema.StringSchema
import java.io.File
import java.util.stream.Stream

/**
 * An interface that handles interactions for string properties with the [com.github.hanseter.json.editor.schemaExtensions.ResourceFormat].
 */
interface ResourceProvider {
    object ResourceProviderEmpty : ResourceProvider {
        override val resourceSelector: ResourceSelector?
            get() = null

        override fun getMatchingResources(
            userText: String,
            objId: String,
            schema: EffectiveSchema<StringSchema>
        ): Stream<String> = Stream.empty()

        override fun isValidResource(
            resource: String?,
            objId: String,
            schema: EffectiveSchema<StringSchema>
        ): Boolean = true
    }

    /**
     * A service to customize the [com.github.hanseter.json.editor.actions.SelectResourceAction].
     */
    interface ResourceSelector {
        /**
         * Returns the initial directory that shall be opened. May return null to use the system default.
         */
        fun getInitialDirectory(schema: EffectiveSchema<StringSchema>): File? = null

        /**
         * A resource was selected via the [com.github.hanseter.json.editor.actions.SelectResourceAction].
         * Returns the identifier of the resource or null if the resource could not be referenced for some reason.
         */
        fun resourceSelected(schema: EffectiveSchema<StringSchema>, file: File): String?
    }

    /**
     * An optional service to customize the [com.github.hanseter.json.editor.actions.SelectResourceAction].
     */
    val resourceSelector: ResourceSelector?

    /**
     * Returns a list of extension filters for this file. By default, checks for a json array under [RESOURCE_EXTENSIONS_KEY].
     * An empty list means all extensions are valid.
     */
    fun getValidExtensions(schema: EffectiveSchema<StringSchema>): List<String> =
        return schema.baseSchema.unprocessedProperties[RESOURCE_EXTENSIONS_KEY] as? List<String>
            ?: emptyList()

    fun getMatchingResources(
        userText: String,
        objId: String,
        schema: EffectiveSchema<StringSchema>,
    ): Stream<String>

    fun isValidResource(
        resource: String?,
        objId: String,
        schema: EffectiveSchema<StringSchema>
    ): Boolean

    companion object {
        const val RESOURCE_EXTENSIONS_KEY = "extensions"
    }
}