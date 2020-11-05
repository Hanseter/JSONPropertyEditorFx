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
import java.lang.reflect.Method


object ControlFactory {

    @Suppress("UNCHECKED_CAST")
    fun convert(schema: SchemaWrapper<*>, context: EditorContext): TypeControl =
            when (val actualSchema = schema.schema) {
                is ObjectSchema -> createObjectControl(schema as SchemaWrapper<ObjectSchema>, context)
                is ArraySchema -> createArrayControl(schema as SchemaWrapper<ArraySchema>, context)
                is BooleanSchema -> createBooleanControl(schema as SchemaWrapper<BooleanSchema>)
                is StringSchema -> createStringControl(schema as SchemaWrapper<StringSchema>, context)
                is NumberSchema -> createNumberControl(schema as SchemaWrapper<NumberSchema>)
                is ReferenceSchema -> convert(ReferredSchemaWrapper(schema as SchemaWrapper<ReferenceSchema>, actualSchema.referredSchema), context)
                is EnumSchema -> createEnumControl(schema, actualSchema)
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

    private fun createBooleanControl(schema: SchemaWrapper<BooleanSchema>) =
            RowBasedControl(BooleanControl(), BooleanModel(schema))

    private fun createStringControl(schema: SchemaWrapper<StringSchema>, context: EditorContext): TypeControl =
            when (schema.schema.formatValidator.formatName()) {
                ColorFormat.formatName -> RowBasedControl(ColorControl(), ColorModel(schema))
                IdReferenceFormat.formatName -> RowBasedControl(IdReferenceControl(schema, context), IdReferenceModel(schema))
                else -> RowBasedControl(StringControl(), StringModel(schema))
            }

    private fun createNumberControl(schema: SchemaWrapper<NumberSchema>): TypeControl =
            if (schema.schema.requiresInteger()) {
                RowBasedControl(IntegerControl(schema.schema), IntegerModel(schema))
            } else {
                RowBasedControl(DoubleControl(schema.schema), DoubleModel(schema))
            }

    @Suppress("UNCHECKED_CAST")
    private fun createEnumControl(schema: SchemaWrapper<out Schema>, enumSchema: EnumSchema): TypeControl {
        val enumModel = EnumModel(schema as SchemaWrapper<Schema>, enumSchema)
        return RowBasedControl<String?>(EnumControl(enumModel), enumModel)
    }

    private fun createCombinedControl(schema: SchemaWrapper<CombinedSchema>, context: EditorContext): TypeControl {
        if (schema.schema.criterion == CombinedSchema.ALL_CRITERION) {
            return createAllOfControl(schema, context)
        }

        return UnsupportedTypeControl(UnsupportedTypeModel(schema))
    }

    private fun createAllOfControl(schema: SchemaWrapper<CombinedSchema>, context: EditorContext): TypeControl {
        if (isSynthetic(schema.schema)) {
            return createControlFromSyntheticAllOf(schema)
        }

        val subSchemas = schema.schema.subschemas.map { CombinedSchemaWrapper(schema, it) }
        val controls = subSchemas.map { convert(it, context) }
        if (controls.any { it !is ObjectControl }) {
            return UnsupportedTypeControl(UnsupportedTypeModel(schema))
        }
        return CombinedObjectControl(CombinedObjectModel(schema), controls as List<ObjectControl>)
    }

    private fun createControlFromSyntheticAllOf(schema: SchemaWrapper<CombinedSchema>): TypeControl {
        val enumSchema = schema.schema.subschemas.find { it is EnumSchema } as? EnumSchema
        if (enumSchema != null) {
            return createEnumControl(schema, enumSchema)
        }
        return UnsupportedTypeControl(UnsupportedTypeModel(schema))
    }


    private val isSyntheticMethod: Method = CombinedSchema::class.java.getDeclaredMethod("isSynthetic").apply { isAccessible = true }
    private fun isSynthetic(combinedSchema: CombinedSchema): Boolean =
            isSyntheticMethod.invoke(combinedSchema) as Boolean

}


