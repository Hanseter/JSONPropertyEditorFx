package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.controls.*
import com.github.hanseter.json.editor.extensions.CombinedSchemaWrapper
import com.github.hanseter.json.editor.extensions.ReferredSchemaWrapper
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.schemaExtensions.ColorFormat
import com.github.hanseter.json.editor.schemaExtensions.IdReferenceFormat
import com.github.hanseter.json.editor.types.*
import com.github.hanseter.json.editor.util.EditorContext
import org.everit.json.schema.*


object ControlFactory {

    @Suppress("UNCHECKED_CAST")
    fun convert(schema: SchemaWrapper<*>, context: EditorContext): TypeControl =
            when (val actualSchema = schema.schema) {
                is ObjectSchema -> createObjectControl(schema as SchemaWrapper<ObjectSchema>, context)
                is ArraySchema -> createArrayControl(schema as SchemaWrapper<ArraySchema>, context)
                is BooleanSchema -> createBooleanControl(schema as SchemaWrapper<BooleanSchema>, context)
                is StringSchema -> createStringControl(schema as SchemaWrapper<StringSchema>, context)
                is NumberSchema -> createNumberControl(schema as SchemaWrapper<NumberSchema>, context)
                is ReferenceSchema -> convert(ReferredSchemaWrapper(schema as SchemaWrapper<ReferenceSchema>, actualSchema.referredSchema), context)
                is EnumSchema -> createEnumControl(schema, actualSchema, context)
                is CombinedSchema -> createCombinedControl(schema as SchemaWrapper<CombinedSchema>, context)
                else -> UnsupportedTypeControl(UnsupportedTypeModel(schema))
            }

    private fun createObjectControl(schema: SchemaWrapper<ObjectSchema>, context: EditorContext) =
            PlainObjectControl(PlainObjectModel(schema), context)

    private fun createArrayControl(schema: SchemaWrapper<ArraySchema>, context: EditorContext) =
            when {
                schema.schema.allItemSchema != null -> ArrayControl(
                        ArrayModel(schema, schema.schema.allItemSchema),
                        context
                )
                schema.schema.itemSchemas != null -> TupleControl(TupleModel(schema, schema.schema.itemSchemas), context)
                else -> throw IllegalArgumentException("Only lists which contain the same type or tuples are supported. Check schema ${schema.schema.schemaLocation}")
            }

    private fun createBooleanControl(schema: SchemaWrapper<BooleanSchema>, context: EditorContext) =
            RowBasedControl(BooleanControl(), BooleanModel(schema), context)

    private fun createStringControl(schema: SchemaWrapper<StringSchema>, context: EditorContext): TypeControl =
            when (schema.schema.formatValidator) {
                ColorFormat.Validator -> RowBasedControl(ColorControl(), ColorModel(schema), context)
                IdReferenceFormat.Validator -> RowBasedControl(IdReferenceControl(schema, context), IdReferenceModel(schema), context)
                else -> RowBasedControl(StringControl(), StringModel(schema), context)
            }

    private fun createNumberControl(schema: SchemaWrapper<NumberSchema>, context: EditorContext): TypeControl =
            if (schema.schema.requiresInteger()) {
                RowBasedControl(IntegerControl(schema.schema), IntegerModel(schema), context)
            } else {
                RowBasedControl(DoubleControl(schema.schema), DoubleModel(schema), context)
            }

    @Suppress("UNCHECKED_CAST")
    private fun createEnumControl(schema: SchemaWrapper<out Schema>, enumSchema: EnumSchema, context: EditorContext): TypeControl {
        val enumModel = EnumModel(schema as SchemaWrapper<Schema>, enumSchema)
        return RowBasedControl(EnumControl(enumModel), enumModel, context)
    }

    private fun createCombinedControl(schema: SchemaWrapper<CombinedSchema>, context: EditorContext): TypeControl {
        if (schema.schema.criterion == CombinedSchema.ALL_CRITERION) {
            return createAllOfControl(schema, context)
        }

        return UnsupportedTypeControl(UnsupportedTypeModel(schema))
    }

    private fun createAllOfControl(schema: SchemaWrapper<CombinedSchema>, context: EditorContext): TypeControl {
        if (CombinedSchemaSyntheticChecker.isSynthetic(schema.schema)) {
            return createControlFromSyntheticAllOf(schema, context)
        }

        val subSchemas = schema.schema.subschemas.map { CombinedSchemaWrapper(schema, it) }
        val controls = subSchemas.map { convert(it, context) }
        if (controls.any { it !is ObjectControl }) {
            return UnsupportedTypeControl(UnsupportedTypeModel(schema))
        }
        return CombinedObjectControl(CombinedObjectModel(schema), controls as List<ObjectControl>, context)
    }

    private fun createControlFromSyntheticAllOf(schema: SchemaWrapper<CombinedSchema>, context: EditorContext): TypeControl {
        val enumSchema = schema.schema.subschemas.find { it is EnumSchema } as? EnumSchema
        if (enumSchema != null) {
            return createEnumControl(schema, enumSchema, context)
        }
        return UnsupportedTypeControl(UnsupportedTypeModel(schema))
    }
}


