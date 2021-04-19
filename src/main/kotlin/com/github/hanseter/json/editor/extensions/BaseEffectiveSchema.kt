package com.github.hanseter.json.editor.extensions

import org.everit.json.schema.JSONPointer
import org.everit.json.schema.ObjectSchema
import org.everit.json.schema.Schema
import org.json.JSONObject

/**
 * Non-generic base interface for [EffectiveSchema]. This should not be used directly, it is only
 * useful for some implementations.
 *
 * In Kotlin 1.5, this interface should become sealed since its only real use is override delegation.
 */
interface BaseEffectiveSchema {

    /**
     * The schema that is used for creating the UI.
     */
    val baseSchema: Schema

    /**
     * The schema that is used for validation purposes.
     */
    val schemaForValidation: Schema
        get() = baseSchema

    val parent: BaseEffectiveSchema?

    val title: String
    val defaultValue: Any?
        get() = baseSchema.defaultValue
    val description: String?
        get() = baseSchema.description
    val readOnly: Boolean
        get() = (baseSchema.isReadOnly ?: parent?.readOnly ?: false)
    val pointer: List<String>
        get() = parent?.pointer?.let { it + propertyName } ?: emptyList()
    val schemaLocation: String?
        get() = findSchemaLocation(this)

    val propertyName: String

    val required: Boolean
        get() = true == objectAncestor?.baseSchema?.requiredProperties?.contains(propertyName)

    val propertyOrder: List<String>
        get() = (baseSchema.unprocessedProperties["order"] as? Iterable<*>)?.filterIsInstance<String>()?.toList()?.distinct()
                ?: emptyList()

    val cssClasses: List<String>
        get() = baseSchema.unprocessedProperties["styleClass"]?.toString()?.split(" ")
                ?: emptyList()

    val cssStyle: String?
        get() = baseSchema.unprocessedProperties["style"]?.toString()

    /**
     * Gets the closest non-synthetic ancestor.
     */
    val nonSyntheticAncestor: BaseEffectiveSchema?

    val objectAncestor: EffectiveSchema<ObjectSchema>?

    fun extractProperty(json: JSONObject): Any? =
            JSONPointer(pointer).queryFrom(json)

    private tailrec fun findSchemaLocation(schema: BaseEffectiveSchema): String? {
        if (schema.baseSchema.schemaLocation != null) return schema.baseSchema.schemaLocation
        val parent = schema.parent ?: return null
        return findSchemaLocation(parent)
    }
}