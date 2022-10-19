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

}

object DefaultCustomizationObject : CustomizationObject