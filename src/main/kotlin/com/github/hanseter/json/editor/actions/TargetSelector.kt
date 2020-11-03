package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.types.SupportedType
import com.github.hanseter.json.editor.types.TypeModel
import org.everit.json.schema.ObjectSchema

/**
 * A target selector is used to specify whether for example an action or a validator should be applied against a specifier subpart of the schema.
 */
fun interface TargetSelector {

    fun matches(model: TypeModel<*, *>): Boolean

    fun invert(): TargetSelector = Inverted(this)

    class Custom(private val predicate: (TypeModel<*, *>) -> Boolean) : TargetSelector {
        override fun matches(model: TypeModel<*, *>) = predicate(model)
    }

    object Always : TargetSelector {
        override fun matches(model: TypeModel<*, *>) = true
    }

    class AllOf(private val selectors: List<TargetSelector>) : TargetSelector {
        override fun matches(model: TypeModel<*, *>) = selectors.all { it.matches(model) }
    }

    class AnyOf(private val selectors: List<TargetSelector>) : TargetSelector {
        override fun matches(model: TypeModel<*, *>) = selectors.any { it.matches(model) }
    }

    class Inverted(private val selector: TargetSelector) : TargetSelector {

        override fun matches(model: TypeModel<*, *>) = !selector.matches(model)

        override fun invert() = selector

    }

    class Single(private val jsonPointerFragment: String) : TargetSelector {

        private fun getFragmentFromJsonPointer(pointer: String): String? {
            val index = pointer.indexOf('#')

            return if (index > -1) pointer.substring(index + 1) else null
        }

        override fun matches(model: TypeModel<*, *>): Boolean {
            val schemaPointer = getFragmentFromJsonPointer(model.schema.schema.schemaLocation)

            return schemaPointer == this.jsonPointerFragment
        }
    }

    object Required : TargetSelector {

        override fun matches(model: TypeModel<*, *>) =
                (model.schema.parent?.schema as? ObjectSchema)?.let {
                    model.schema.getPropertyName() in it.requiredProperties
                } ?: true

    }

    object ReadOnly : TargetSelector {
        override fun matches(model: TypeModel<*, *>) =
                model.schema.readOnly
    }

    class SchemaType(private vararg val types: SupportedType<*>) : TargetSelector {

        override fun matches(model: TypeModel<*, *>): Boolean =
                types.contains(model.supportedType)

    }

    class HasCustomField(private val fieldName: String, private val value: Any? = null) : TargetSelector {

        override fun matches(model: TypeModel<*, *>): Boolean =
                model.schema.schema.unprocessedProperties.containsKey(fieldName)
                        && (null == value || model.schema.schema.unprocessedProperties[fieldName] == value)

    }

}