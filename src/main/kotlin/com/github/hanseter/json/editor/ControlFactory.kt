package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.controls.*
import com.github.hanseter.json.editor.extensions.ReferredSchemaWrapper
import com.github.hanseter.json.editor.extensions.RegularSchemaWrapper
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.schemaExtensions.ColorFormat
import com.github.hanseter.json.editor.schemaExtensions.IdReferenceFormat
import org.everit.json.schema.*

object ControlFactory {

    @Suppress("UNCHECKED_CAST")
    fun convert(schema: SchemaWrapper<*>, refProvider: IdReferenceProposalProvider, resolutionScopeProvider: ResolutionScopeProvider): TypeControl =
            when (val actualSchema = schema.schema) {
                is ObjectSchema -> createObjectControl(schema as SchemaWrapper<ObjectSchema>, refProvider, resolutionScopeProvider)
                is ArraySchema -> createArrayControl(schema as SchemaWrapper<ArraySchema>, refProvider, resolutionScopeProvider)
                is BooleanSchema -> createBooleanControl(schema as SchemaWrapper<BooleanSchema>)
                is StringSchema -> createStringControl(schema as SchemaWrapper<StringSchema>, refProvider, resolutionScopeProvider)
                is NumberSchema -> createNumberControl(schema as SchemaWrapper<NumberSchema>)
                is ReferenceSchema -> convert(ReferredSchemaWrapper(schema as SchemaWrapper<ReferenceSchema>, actualSchema.referredSchema), refProvider, resolutionScopeProvider)
                is EnumSchema -> createEnumControl(schema, actualSchema)
                is CombinedSchema -> createCombinedControl(schema as SchemaWrapper<CombinedSchema>, refProvider, resolutionScopeProvider)
                else -> UnsupportedTypeControl(schema)
            }

    private fun createObjectControl(schema: SchemaWrapper<ObjectSchema>,
                                    refProvider: IdReferenceProposalProvider,
                                    resolutionScopeProvider: ResolutionScopeProvider
    ) = PlainObjectControl(schema, refProvider, resolutionScopeProvider)

    private fun createArrayControl(schema: SchemaWrapper<ArraySchema>, refProvider: IdReferenceProposalProvider, resolutionScopeProvider: ResolutionScopeProvider) =
            when {
                schema.schema.allItemSchema != null -> ArrayControl(
                        schema,
                        schema.schema.allItemSchema,
                        refProvider,
                        resolutionScopeProvider
                )
                schema.schema.itemSchemas != null -> TupleControl(schema, schema.schema.itemSchemas, refProvider, resolutionScopeProvider)
                else -> throw IllegalArgumentException("Only lists which contain the same type or tuples are supported. Check schema ${schema.schema.schemaLocation}")
            }

    private fun createBooleanControl(schema: SchemaWrapper<BooleanSchema>) = BooleanControl(schema)

    private fun createStringControl(schema: SchemaWrapper<StringSchema>,
                                    refProvider: IdReferenceProposalProvider,
                                    resolutionScopeProvider: ResolutionScopeProvider
    ) = when (schema.schema.formatValidator) {
        ColorFormat.Validator -> ColorControl(schema)
        IdReferenceFormat.Validator -> IdReferenceControl(schema, refProvider, resolutionScopeProvider)
        else -> StringControl(schema)
    }

    private fun createNumberControl(schema: SchemaWrapper<NumberSchema>) =
            if (schema.schema.requiresInteger()) {
                IntegerControl(schema)
            } else {
                DoubleControl(schema)
            }

    @Suppress("UNCHECKED_CAST")
    private fun createEnumControl(schema: SchemaWrapper<out Schema>, enumSchema: EnumSchema) =
            EnumControl(schema as SchemaWrapper<Schema>, enumSchema)

    private fun createCombinedControl(schema: SchemaWrapper<CombinedSchema>, refProvider: IdReferenceProposalProvider, resolutionScopeProvider: ResolutionScopeProvider): TypeControl {
        if (schema.schema.criterion == CombinedSchema.ALL_CRITERION) {
            return createAllOfControl(schema, refProvider, resolutionScopeProvider)
        }
        val enumSchema = schema.schema.subschemas.find { it is EnumSchema } as? EnumSchema
        if (enumSchema != null) {
            return createEnumControl(schema, enumSchema)
        }
        return UnsupportedTypeControl(schema)
    }

    private fun createAllOfControl(schema: SchemaWrapper<CombinedSchema>, refProvider: IdReferenceProposalProvider, resolutionScopeProvider: ResolutionScopeProvider): TypeControl {
        val subSchemas = schema.schema.subschemas.map { RegularSchemaWrapper(schema, it) }
        val controls = subSchemas.map { convert(it, refProvider, resolutionScopeProvider) }
        if (controls.any {it !is ObjectControl}) {
            return UnsupportedTypeControl(schema)
        }
        return CombinedObjectControl(schema, controls as List<ObjectControl>)
    }
}