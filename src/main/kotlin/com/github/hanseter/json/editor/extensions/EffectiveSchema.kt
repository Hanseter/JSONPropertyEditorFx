package com.github.hanseter.json.editor.extensions

import org.everit.json.schema.JSONPointer
import org.everit.json.schema.Schema
import org.json.JSONObject

/**
 * Wraps a [Schema] and provides getters for some properties.
 * These getters should be used whenever possible instead of getting these values from the base schema, as they might adapt some values.
 */
interface EffectiveSchema<T : Schema> {
    val parent: EffectiveSchema<*>?
    val baseSchema: T
    val defaultValue: Any?
        get() = baseSchema.defaultValue
    val title: String
    val description: String?
        get() = baseSchema.description
    val readOnly: Boolean
        get() = (baseSchema.isReadOnly ?: parent?.readOnly ?: false)
    val pointer: List<String>
        get() = parent?.pointer?.let { it + getPropertyName() } ?: emptyList()

    fun getPropertyName(): String

    fun getPropertyOrder(): List<String> = (baseSchema.unprocessedProperties["order"] as? Iterable<*>)?.filterIsInstance<String>()?.toList()?.distinct()
            ?: emptyList()


    fun extractProperty(json: JSONObject): Any? =
            JSONPointer(pointer).queryFrom(json)
}