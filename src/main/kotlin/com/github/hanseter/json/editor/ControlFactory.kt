package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.actions.EditorAction
import com.github.hanseter.json.editor.controls.*
import com.github.hanseter.json.editor.extensions.ReferredSchemaWrapper
import com.github.hanseter.json.editor.extensions.RegularSchemaWrapper
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.schemaExtensions.ColorFormat
import com.github.hanseter.json.editor.schemaExtensions.IdReferenceFormat
import org.everit.json.schema.*

object ControlFactory {

    @Suppress("UNCHECKED_CAST")
    fun convert(schema: SchemaWrapper<*>,
                refProvider: IdReferenceProposalProvider,
                resolutionScopeProvider: ResolutionScopeProvider,
                actions: List<EditorAction>): TypeControl =
            when (val actualSchema = schema.schema) {
                is ObjectSchema -> createObjectControl(schema as SchemaWrapper<ObjectSchema>, refProvider, resolutionScopeProvider, actions)
                is ArraySchema -> createArrayControl(schema as SchemaWrapper<ArraySchema>, refProvider, resolutionScopeProvider, actions)
                is BooleanSchema -> createBooleanControl(schema as SchemaWrapper<BooleanSchema>, actions)
                is StringSchema -> createStringControl(schema as SchemaWrapper<StringSchema>, refProvider, resolutionScopeProvider, actions)
                is NumberSchema -> createNumberControl(schema as SchemaWrapper<NumberSchema>, actions)
                is ReferenceSchema -> convert(ReferredSchemaWrapper(schema as SchemaWrapper<ReferenceSchema>, actualSchema.referredSchema), refProvider, resolutionScopeProvider, actions)
                is EnumSchema -> createEnumControl(schema, actualSchema, actions)
                is CombinedSchema -> createCombinedControl(schema as SchemaWrapper<CombinedSchema>, refProvider, resolutionScopeProvider, actions)
                else -> UnsupportedTypeControl(schema)
            }

    private fun createObjectControl(schema: SchemaWrapper<ObjectSchema>,
                                    refProvider: IdReferenceProposalProvider,
                                    resolutionScopeProvider: ResolutionScopeProvider,
                                    actions: List<EditorAction>
    ) = PlainObjectControl(schema, refProvider, resolutionScopeProvider, actions)

    private fun createArrayControl(schema: SchemaWrapper<ArraySchema>, refProvider: IdReferenceProposalProvider, resolutionScopeProvider: ResolutionScopeProvider, actions: List<EditorAction>) =
            when {
                schema.schema.allItemSchema != null -> ArrayControl(
                        schema,
                        schema.schema.allItemSchema,
                        refProvider,
                        resolutionScopeProvider,
                        actions
                )
                schema.schema.itemSchemas != null -> TupleControl(schema, schema.schema.itemSchemas, refProvider, resolutionScopeProvider, actions)
                else -> throw IllegalArgumentException("Only lists which contain the same type or tuples are supported. Check schema ${schema.schema.schemaLocation}")
            }

    private fun createBooleanControl(schema: SchemaWrapper<BooleanSchema>, actions: List<EditorAction>) = BooleanControl(schema, actions)

    private fun createStringControl(schema: SchemaWrapper<StringSchema>,
                                    refProvider: IdReferenceProposalProvider,
                                    resolutionScopeProvider: ResolutionScopeProvider,
                                    actions: List<EditorAction>
    ) = when (schema.schema.formatValidator) {
        ColorFormat.Validator -> ColorControl(schema, actions)
        IdReferenceFormat.Validator -> IdReferenceControl(schema, refProvider, resolutionScopeProvider, actions)
        else -> StringControl(schema, actions)
    }

    private fun createNumberControl(schema: SchemaWrapper<NumberSchema>, actions: List<EditorAction>) =
            if (schema.schema.requiresInteger()) {
                IntegerControl(schema, actions)
            } else {
                DoubleControl(schema, actions)
            }

    @Suppress("UNCHECKED_CAST")
    private fun createEnumControl(schema: SchemaWrapper<out Schema>, enumSchema: EnumSchema, actions: List<EditorAction>) =
            EnumControl(schema as SchemaWrapper<Schema>, enumSchema, actions)

    private fun createCombinedControl(schema: SchemaWrapper<CombinedSchema>,
                                      refProvider: IdReferenceProposalProvider,
                                      resolutionScopeProvider: ResolutionScopeProvider,
                                      actions: List<EditorAction>): TypeControl {
        if (schema.schema.criterion == CombinedSchema.ALL_CRITERION) {
            return createAllOfControl(schema, refProvider, resolutionScopeProvider, actions)
        }

        return UnsupportedTypeControl(schema)
    }

    private fun createAllOfControl(schema: SchemaWrapper<CombinedSchema>,
                                   refProvider: IdReferenceProposalProvider,
                                   resolutionScopeProvider: ResolutionScopeProvider,
                                   actions: List<EditorAction>): TypeControl {
        if (CombinedSchemaSyntheticChecker.isSynthetic(schema.schema)) {
            return createControlFromSyntheticAllOf(schema, actions)
        }

        val subSchemas = schema.schema.subschemas.map { RegularSchemaWrapper(schema, it) }
        val controls = subSchemas.map { convert(it, refProvider, resolutionScopeProvider, actions) }
        if (controls.any { it !is ObjectControl }) {
            return UnsupportedTypeControl(schema)
        }
        return CombinedObjectControl(schema, controls as List<ObjectControl>, actions)
    }

    private fun createControlFromSyntheticAllOf(schema: SchemaWrapper<CombinedSchema>, actions: List<EditorAction>) : TypeControl {
        val enumSchema = schema.schema.subschemas.find { it is EnumSchema } as? EnumSchema
        if (enumSchema != null) {
            return createEnumControl(schema, enumSchema, actions)
        }
        return UnsupportedTypeControl(schema)
    }
}