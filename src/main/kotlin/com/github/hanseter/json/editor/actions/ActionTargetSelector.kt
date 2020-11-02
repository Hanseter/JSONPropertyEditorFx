package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.types.SupportedType
import com.github.hanseter.json.editor.types.TypeModel
import org.everit.json.schema.ObjectSchema

/**
 *
 */
fun interface ActionTargetSelector {

    fun matches(model: TypeModel<*, *>): Boolean

    fun invert(): ActionTargetSelector = Inverted(this)

    class Custom(private val predicate: (TypeModel<*, *>) -> Boolean) : ActionTargetSelector {
        override fun matches(model: TypeModel<*, *>) = predicate(model)
    }

    object Always : ActionTargetSelector {
        override fun matches(model: TypeModel<*, *>) = true
    }

    class AllOf(private val selectors: List<ActionTargetSelector>) : ActionTargetSelector {
        override fun matches(model: TypeModel<*, *>) = selectors.all { it.matches(model) }
    }

    class AnyOf(private val selectors: List<ActionTargetSelector>) : ActionTargetSelector {
        override fun matches(model: TypeModel<*, *>) = selectors.any { it.matches(model) }
    }

    class Inverted(private val selector: ActionTargetSelector) : ActionTargetSelector {

        override fun matches(model: TypeModel<*, *>) = !selector.matches(model)

        override fun invert() = selector

    }

    class Single(private val jsonPointerFragment: String) : ActionTargetSelector {

        private fun getFragmentFromJsonPointer(pointer: String): String? {
            val index = pointer.indexOf('#')

            return if (index > -1) pointer.substring(index + 1) else null
        }

        override fun matches(model: TypeModel<*, *>): Boolean {
            val schemaPointer = getFragmentFromJsonPointer(model.schema.schema.schemaLocation)

            return schemaPointer == this.jsonPointerFragment
        }
    }

    object Required : ActionTargetSelector {

        override fun matches(model: TypeModel<*, *>) =
                (model.schema.parent?.schema as? ObjectSchema)?.let {
                    model.schema.getPropertyName() in it.requiredProperties
                } ?: true

    }

    object ReadOnly : ActionTargetSelector {
        override fun matches(model: TypeModel<*, *>) =
                model.schema.readOnly
    }

    class SchemaType(private vararg val types: SupportedType<*>) : ActionTargetSelector {

        override fun matches(model: TypeModel<*, *>): Boolean =
                types.contains(model.supportedType)

    }

    class HasCustomField(private val fieldName: String, private val value: Any? = null) : ActionTargetSelector {

        override fun matches(model: TypeModel<*, *>): Boolean =
                model.schema.schema.unprocessedProperties.containsKey(fieldName)
                        && (null == value || model.schema.schema.unprocessedProperties[fieldName] == value)

    }

}