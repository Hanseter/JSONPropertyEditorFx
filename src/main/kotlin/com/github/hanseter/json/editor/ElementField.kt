package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.validators.JSONPointer

/**
 * Identifies the field of an element. Used in the selection model and other methods of the [JsonPropertiesEditor]
 */
data class ElementField(val elementId: String, val fieldPointer: JSONPointer)