package com.github.hanseter.json.editor

import org.everit.json.schema.ObjectSchema
import javafx.scene.layout.HBox
import javafx.scene.control.Label
import javafx.scene.Node
import org.everit.json.schema.BooleanSchema
import javafx.scene.control.Control
import javafx.scene.layout.Priority
import org.controlsfx.control.ToggleSwitch
import org.everit.json.schema.Schema
import javafx.scene.control.Tooltip
import org.everit.json.schema.StringSchema
import javafx.scene.control.TextField
import org.controlsfx.control.textfield.TextFields
import org.controlsfx.validation.ValidationSupport
import com.github.hanseter.json.editor.controls.StringControl
import com.github.hanseter.json.editor.controls.BooleanControl
import com.github.hanseter.json.editor.controls.ObjectControl
import org.everit.json.schema.ArraySchema
import com.github.hanseter.json.editor.controls.ArrayControl
import com.github.hanseter.json.editor.controls.TupleControl
import org.everit.json.schema.NumberSchema
import com.github.hanseter.json.editor.controls.IntegerControl
import com.github.hanseter.json.editor.controls.DoubleControl
import com.github.hanseter.json.editor.controls.TypeControl
import com.github.hanseter.json.editor.controls.UnsupportedTypeControl
import com.github.hanseter.json.editor.schemaExtensions.ColorFormat
import com.github.hanseter.json.editor.controls.ColorControl
import org.everit.json.schema.ReferenceSchema
import com.github.hanseter.json.editor.schemaExtensions.IdReferenceFormat
import com.github.hanseter.json.editor.controls.IdReferenceControl
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import org.everit.json.schema.EnumSchema
import org.everit.json.schema.CombinedSchema
import com.github.hanseter.json.editor.controls.EnumControl

object ControlFactory {

	@Suppress("UNCHECKED_CAST")
	fun convert(schema: SchemaWrapper<*>, refProvider: IdReferenceProposalProvider): TypeControl =
		when (schema.schema) {
			is ObjectSchema -> createObjectControl(schema as SchemaWrapper<ObjectSchema>, refProvider)
			is ArraySchema -> createArrayControl(schema as SchemaWrapper<ArraySchema>, refProvider)
			is BooleanSchema -> createBooleanControl(schema as SchemaWrapper<BooleanSchema>)
			is StringSchema -> createStringControl(schema as SchemaWrapper<StringSchema>, refProvider)
			is NumberSchema -> createNumberControl(schema as SchemaWrapper<NumberSchema>)
			is ReferenceSchema -> convert(SchemaWrapper(schema.parent, schema.schema.getReferredSchema()), refProvider)
			is EnumSchema -> createEnumControl(schema, schema.schema)
			is CombinedSchema -> createCombinedControl(schema as SchemaWrapper<CombinedSchema>)
			else -> UnsupportedTypeControl(schema)
		}

	private fun createObjectControl(schema: SchemaWrapper<ObjectSchema>, refProvider: IdReferenceProposalProvider) =
		ObjectControl(schema, refProvider)

	private fun createArrayControl(schema: SchemaWrapper<ArraySchema>, refProvider: IdReferenceProposalProvider) =
		when {
			schema.schema.getAllItemSchema() != null -> ArrayControl(
				schema,
				schema.schema.getAllItemSchema(),
				refProvider
			)
			schema.schema.getItemSchemas() != null -> TupleControl(schema, schema.schema.getItemSchemas(), refProvider)
			else -> throw IllegalArgumentException("Only lists which contain the same type or tuples are supported. Check schema ${schema.schema.getSchemaLocation()}")
		}

	private fun createBooleanControl(schema: SchemaWrapper<BooleanSchema>) = BooleanControl(schema)

	private fun createStringControl(schema: SchemaWrapper<StringSchema>, refProvider: IdReferenceProposalProvider) =
		when (schema.schema.getFormatValidator()) {
			ColorFormat.Validator -> ColorControl(schema)
			IdReferenceFormat.Validator -> IdReferenceControl(schema, refProvider)
			else -> StringControl(schema)
		}

	private fun createNumberControl(schema: SchemaWrapper<NumberSchema>) = if (schema.schema.requiresInteger()) {
		IntegerControl(schema)
	} else {
		DoubleControl(schema)
	}

	private fun createEnumControl(schema: SchemaWrapper<out Schema>, enumSchema: EnumSchema) =
		EnumControl(schema as SchemaWrapper<Schema>, enumSchema)

	private fun createCombinedControl(schema: SchemaWrapper<CombinedSchema>): TypeControl {
		val enumSchema = schema.schema.subschemas.find { it is EnumSchema } as? EnumSchema
		if (enumSchema != null) {
			return createEnumControl(schema, enumSchema)
		}
		return UnsupportedTypeControl(schema)
	}

}