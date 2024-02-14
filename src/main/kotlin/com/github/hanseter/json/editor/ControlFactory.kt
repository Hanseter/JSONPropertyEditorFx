package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.controls.*
import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.extensions.ForceReadOnlyEffectiveSchema
import com.github.hanseter.json.editor.extensions.PartialEffectiveSchema
import com.github.hanseter.json.editor.schemaExtensions.ColorFormat
import com.github.hanseter.json.editor.schemaExtensions.IdReferenceFormat
import com.github.hanseter.json.editor.schemaExtensions.LocalTimeFormat
import com.github.hanseter.json.editor.schemaExtensions.synthetic
import com.github.hanseter.json.editor.types.*
import com.github.hanseter.json.editor.types.FormattedIntegerModel.Companion.INT_FORMAT
import com.github.hanseter.json.editor.util.EditorContext
import org.everit.json.schema.*
import org.slf4j.LoggerFactory


object ControlFactory {

    private val LOG = LoggerFactory.getLogger(ControlFactory::class.java)

    @Suppress("UNCHECKED_CAST")
    fun convert(schema: EffectiveSchema<*>, context: EditorContext): TypeControl =
        when (val actualSchema = schema.baseSchema) {
            is ObjectSchema -> createObjectControl(schema as EffectiveSchema<ObjectSchema>, context)
            is ArraySchema -> createArrayControl(schema as EffectiveSchema<ArraySchema>, context)
            is BooleanSchema -> createBooleanControl(schema as EffectiveSchema<BooleanSchema>)
            is StringSchema -> createStringControl(schema as EffectiveSchema<StringSchema>, context)
            is NumberSchema -> createNumberControl(schema as EffectiveSchema<NumberSchema>,context)
            is EnumSchema -> createEnumControl(schema, actualSchema)
            is CombinedSchema -> createCombinedControl(
                schema as EffectiveSchema<CombinedSchema>,
                context
            )

            is ConstSchema -> createConstControl(schema as EffectiveSchema<ConstSchema>)
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
                IdReferenceModel(schema,context)
            )

            LocalTimeFormat.formatName -> RowBasedControl(
                { LocalTimeControl() },
                LocalTimeModel(schema)
            )

            "date" -> RowBasedControl({ DateControl() }, DateModel(schema))
            else -> RowBasedControl({ StringControl() }, StringModel(schema))
        }

    private fun createNumberControl(schema: EffectiveSchema<NumberSchema>, context: EditorContext): TypeControl =
        if (schema.baseSchema.requiresInteger()) {
            if (schema.baseSchema.unprocessedProperties.keys.contains(INT_FORMAT)) {
                val model=FormattedIntegerModel(schema,context.decimalFormatSymbols)
                RowBasedControl({ FormattedIntegerControl(model) },model )
            } else RowBasedControl({ IntegerControl() }, IntegerModel(schema))
        } else {
            RowBasedControl({ DoubleControl(context.decimalFormatSymbols) }, DoubleModel(schema))
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
        schema.baseSchema.subschemas.firstOrNull()?.let discriminated@{ firstSchema ->
            // Create a Discriminated Union model under the following conditions:
            // - every subschema is an object
            // - a given discriminator property with a const value is present and required in every subschema
            // - the const value for that discriminator is different in every subschema

            if (firstSchema is ObjectSchema) {
                firstSchema.propertySchemas.keys.filter {
                    DiscriminatedOneOfModel.getDiscriminatorSchema(firstSchema, it) != null
                }.forEach { discriminator ->
                    val constSchemas = schema.baseSchema.subschemas.map {
                        DiscriminatedOneOfModel.getDiscriminatorSchema(it, discriminator)
                            ?: return@discriminated
                    }

                    if (constSchemas.size == constSchemas.distinctBy { it.permittedValue }.size) {
                        return OneOfControl(
                            DiscriminatedOneOfModel(schema, context, discriminator)
                        )
                    }
                }
            }
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

    private fun createConstControl(schema: EffectiveSchema<ConstSchema>): TypeControl {
        return RowBasedControl({ ConstControl() }, UnsupportedTypeModel(schema))
    }

    private fun getSingleUiSchema(schema: EffectiveSchema<CombinedSchema>): EffectiveSchema<*>? {
        val uiSchemas = schema.baseSchema.subschemas.filter {
            !(it is NotSchema || it is ConditionalSchema || it is TrueSchema || it is NullSchema)
        }

        if (uiSchemas.size == 2) {
            val constSchema = uiSchemas.firstOrNull { it is ConstSchema }
            val nonConstSchema = uiSchemas.firstOrNull { it !is ConstSchema }

            if (constSchema != null && nonConstSchema != null) {
                return ForceReadOnlyEffectiveSchema(PartialEffectiveSchema(schema, nonConstSchema))
            }
        }

        val onlySchema = uiSchemas.singleOrNull() ?: return null


        return PartialEffectiveSchema(schema, onlySchema)
    }
}


