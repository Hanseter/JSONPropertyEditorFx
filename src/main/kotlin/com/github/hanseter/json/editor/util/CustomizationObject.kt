package com.github.hanseter.json.editor.util

import com.github.hanseter.json.editor.types.TypeModel

/**
 * An object that can be used to customize the behavior and appearance of the editor.
 */
interface CustomizationObject {

    /**
     * Gets the title for a given element.
     */
    fun getTitle(model: TypeModel<*, *>, defaultTitle: String): String {
        return defaultTitle
    }

    /**
     * Gets the description for a given element.
     */
    fun getDescription(model: TypeModel<*, *>, defaultDescription: String?): String? {
        return defaultDescription
    }

}

object DefaultCustomizationObject : CustomizationObject