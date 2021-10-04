package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.controls.*
import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.extensions.PartialEffectiveSchema
import com.github.hanseter.json.editor.schemaExtensions.ColorFormat
import com.github.hanseter.json.editor.schemaExtensions.IdReferenceFormat
import com.github.hanseter.json.editor.schemaExtensions.LocalTimeFormat
import com.github.hanseter.json.editor.schemaExtensions.synthetic
import com.github.hanseter.json.editor.types.*
import com.github.hanseter.json.editor.util.EditorContext
import org.everit.json.schema.*


object ControlFactory {

    @Suppress("UNCHECKED_CAST")
    fun convert(schema: EffectiveSchema<*>, context: EditorContext): TypeControl =
        when (val actualSchema = schema.baseSchema) {
            is ObjectSchema -> createObjectControl(schema as EffectiveSchema<ObjectSchema>, context)
            is ArraySchema -> createArrayControl(schema as EffectiveSchema<ArraySchema>, context)
            is BooleanSchema -> createBooleanControl(schema as EffectiveSchema<BooleanSchema>)
            is StringSchema -> createStringControl(schema as EffectiveSchema<StringSchema>, context)
            is NumberSchema -> createNumberControl(schema as EffectiveSchema<NumberSchema>)
            is EnumSchema -> createEnumControl(schema, actualSchema)
            is CombinedSchema -> createCombinedControl(
                schema as EffectiveSchema<CombinedSchema>,
                context
            )
            else -> UnsupportedTypeControl(UnsupportedTypeModel(schema))
        }

    private fun createObjectControl(schema: EffectiveSchema<ObjectSchema>, context: EditorContext) =
        PlainObjectControl(PlainObjectModel(schema), context)

    private fun createArrayControl(schema: EffectiveSchema<ArraySchema>, context: EditorContext) =
        when {
            schema.baseSchema.allItemSchema != null -> ArrayControl(
                ArrayModel(schema, schema.baseSchema.allItemSchema),
                context
            )
            schema.baseSchema.itemSchemas != null -> TupleControl(
                TupleModel(
                    schema,
                    schema.baseSchema.itemSchemas
                ), context
            )
            else -> throw IllegalArgumentException("Only lists which contain the same type or tuples are supported. Check schema ${schema.baseSchema.schemaLocation}")
        }

    private fun createBooleanControl(schema: EffectiveSchema<BooleanSchema>) =
        RowBasedControl({ BooleanControl() }, BooleanModel(schema))

    private fun createStringControl(
        schema: EffectiveSchema<StringSchema>,
        context: EditorContext
    ): TypeControl =
        when (schema.baseSchema.formatValidator.formatName()) {
            ColorFormat.formatName -> RowBasedControl({ ColorControl() }, ColorModel(schema))
            IdReferenceFormat.formatName -> RowBasedControl(
                { IdReferenceControl(schema, context) },
                IdReferenceModel(schema)
            )
            LocalTimeFormat.formatName -> RowBasedControl(
                { LocalTimeControl() },
                LocalTimeModel(schema)
            )
            "date" -> RowBasedControl({ DateControl() }, DateModel(schema))
            else -> RowBasedControl({ StringControl() }, StringModel(schema))
        }

    private fun createNumberControl(schema: EffectiveSchema<NumberSchema>): TypeControl =
        if (schema.baseSchema.requiresInteger()) {
            RowBasedControl({ IntegerControl(schema.baseSchema) }, IntegerModel(schema))
        } else {
            RowBasedControl({ DoubleControl(schema.baseSchema) }, DoubleModel(schema))
        }

    @Suppress("UNCHECKED_CAST")
    private fun createEnumControl(
        schema: EffectiveSchema<out Schema>,
        enumSchema: EnumSchema
    ): TypeControl {

        val baseSchema = schema.baseSchema
        val effectiveSchema: EffectiveSchema<Schema> = if (null in enumSchema.possibleValues) {
            PartialEffectiveSchema(schema as EffectiveSchema<CombinedSchema>, baseSchema)
        } else {
            schema as EffectiveSchema<Schema>
        }

        val enumModel = EnumModel(effectiveSchema, enumSchema)
        return RowBasedControl<String?>({ EnumControl(enumModel) }, enumModel)
    }

    private fun createCombinedControl(
        schema: EffectiveSchema<CombinedSchema>,
        context: EditorContext
    ): TypeControl =
        when (schema.baseSchema.criterion) {
            CombinedSchema.ALL_CRITERION -> createAllOfControl(schema, context)
            CombinedSchema.ONE_CRITERION -> createOneOfControl(schema, context)
            CombinedSchema.ANY_CRITERION -> createAnyOfControl(schema, context)
            else -> UnsupportedTypeControl(UnsupportedTypeModel(schema))
        }

    private fun createAllOfControl(
        schema: EffectiveSchema<CombinedSchema>,
        context: EditorContext
    ): TypeControl {
        if (schema.baseSchema.synthetic) {
            return createControlFromSyntheticAllOf(schema, context)
        }
        return UnsupportedTypeControl(UnsupportedTypeModel(schema))
    }

    private fun createControlFromSyntheticAllOf(
        schema: EffectiveSchema<CombinedSchema>,
        context: EditorContext
    ): TypeControl {
        val enumSchema = schema.baseSchema.subschemas.find { it is EnumSchema } as? EnumSchema
        if (enumSchema != null) {
            return createEnumControl(schema, enumSchema)
        }
        getSingleUiSchema(schema)?.let {
            return convert(it, context)
        }

        return UnsupportedTypeControl(UnsupportedTypeModel(schema))
    }

    private fun createOneOfControl(
        schema: EffectiveSchema<CombinedSchema>,
        context: EditorContext
    ): TypeControl {
        schema.baseSchema.subschemas.firstOrNull()?.let { firstSchema ->
            if (firstSchema is ObjectSchema) {
                val discriminator = firstSchema.propertySchemas.firstNotNullOfOrNull {
                    if (
                        DiscriminatedOneOfModel.getDiscriminatorSchema(firstSchema, it.key) != null
                    ) it.key else null
                }

                if (discriminator != null
                    && schema.baseSchema.subschemas.all {
                        DiscriminatedOneOfModel.getDiscriminatorSchema(it, discriminator) != null
                    }
                ) {

                    val constSchemas = schema.baseSchema.subschemas.map {
                        DiscriminatedOneOfModel.getDiscriminatorSchema(it, discriminator)!!
                    }

                    val constValues = constSchemas.distinctBy { it.permittedValue }

                    if (constSchemas.size == constValues.size) {
                        return OneOfControl(DiscriminatedOneOfModel(schema, context, discriminator))
                    }
                }
            }
            val foo = true
        }

        return OneOfControl(OneOfModel(schema, context))
    }

    private fun createAnyOfControl(
        schema: EffectiveSchema<CombinedSchema>,
        context: EditorContext
    ): TypeControl {
        getSingleUiSchema(schema)?.let {
            return convert(it, context)
        }

        return UnsupportedTypeControl(UnsupportedTypeModel(schema))
    }

    private fun getSingleUiSchema(schema: EffectiveSchema<CombinedSchema>): EffectiveSchema<*>? {
        val onlySchema = schema.baseSchema.subschemas.singleOrNull {
            !(it is NotSchema || it is ConditionalSchema || it is TrueSchema || it is NullSchema)
        } ?: return null

        return PartialEffectiveSchema(schema, onlySchema)
    }
}


