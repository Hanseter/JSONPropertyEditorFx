package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import org.everit.json.schema.*
import java.util.function.Predicate

/**
 *
 */
interface ActionTargetSelector {

    fun matches(schema: SchemaWrapper<*>): Boolean

    fun invert(): ActionTargetSelector = Inverted(this)

    class Custom(private val predicate: Predicate<SchemaWrapper<*>>) : ActionTargetSelector {
        override fun matches(schema: SchemaWrapper<*>) = predicate.test(schema)
    }

    class Always : ActionTargetSelector {
        override fun matches(schema: SchemaWrapper<*>) = true
    }

    class AllOf(private val selectors: List<ActionTargetSelector>) : ActionTargetSelector {
        override fun matches(schema: SchemaWrapper<*>) = selectors.all { it.matches(schema) }
    }

    class AnyOf(private val selectors: List<ActionTargetSelector>) : ActionTargetSelector {
        override fun matches(schema: SchemaWrapper<*>) = selectors.any { it.matches(schema) }
    }

    class Inverted(private val selector: ActionTargetSelector) : ActionTargetSelector {

        override fun matches(schema: SchemaWrapper<*>) = !selector.matches(schema)

        override fun invert() = selector

    }

    class Single(private val jsonPointerFragment: String) : ActionTargetSelector {

        private fun getFragmentFromJsonPointer(pointer: String): String? {
            val index = pointer.indexOf('#')

            return if (index > -1) pointer.substring(index + 1) else null
        }

        override fun matches(schema: SchemaWrapper<*>): Boolean {
            val schemaPointer = getFragmentFromJsonPointer(schema.schema.schemaLocation)

            return schemaPointer == this.jsonPointerFragment
        }
    }

    class Required : ActionTargetSelector {

        override fun matches(schema: SchemaWrapper<*>) =
                (schema.parent?.schema as? ObjectSchema)?.let {
                    schema.getPropertyName() in it.requiredProperties
                } ?: true

    }

    class ReadOnly : ActionTargetSelector {
        override fun matches(schema: SchemaWrapper<*>) =
                schema.readOnly
    }

    class SchemaType(private vararg val types: String) : ActionTargetSelector {

        private fun matches(schema: Schema): Boolean {
            return when (schema) {
                is StringSchema -> types.contains("string")
                is ArraySchema -> types.contains("array")
                is BooleanSchema -> types.contains("boolean")
                is NumberSchema -> types.contains(if (schema.requiresInteger()) "integer" else "number")
                is ObjectSchema -> types.contains("object")
                is NullSchema -> types.contains("null")
                is EnumSchema -> types.contains("string")
                else -> false
            }
        }

        override fun matches(schema: SchemaWrapper<*>): Boolean {
            return matches(schema.schema)
        }

    }

    class HasCustomField(private val fieldName: String, private val value: Any? = null) : ActionTargetSelector {

        override fun matches(schema: SchemaWrapper<*>): Boolean {
            return schema.schema.unprocessedProperties.containsKey(fieldName)
                    && (null == value || schema.schema.unprocessedProperties[fieldName] == value)
        }

    }

}