package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.validators.JSONPointer
import org.json.JSONObject

/**
 * The result of a user function to change the editor's state.
 */
data class PropertiesEditResult @JvmOverloads constructor(

    /**
     * The new document data.
     */
    val data: JSONObject? = null,

    /**
     * The new schema, or `null` if the schema shouldn't change.
     *
     * A non-null value should only be passed here if the schema should actually change; if the schema hasn't changed, `null` should be passed here, not the old schema.
     */
    val schema: ParsedSchema? = null,

    /**
     * A callback to run on the properties editor after the result has been applied.
     */
    val postUpdateHandler: ((data: PostUpdateHandlerData) -> Unit)? = null,
) {

    interface PostUpdateHandlerData {

        val editor: JsonPropertiesEditor

        val elementId: String

        fun focusField(pointer: JSONPointer) {
            editor.focusField(ElementField(elementId, pointer))
        }

    }

}
