package com.github.hanseter.json.editor

import org.junit.jupiter.api.Test
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.everit.json.schema.ObjectSchema
import org.everit.json.schema.Schema
import org.json.JSONTokener
import org.everit.json.schema.loader.SchemaLoader.SchemaLoaderBuilder
import org.everit.json.schema.loader.SchemaClient
import org.everit.json.schema.loader.internal.DefaultSchemaClient
import org.everit.json.schema.StringSchema
import org.everit.json.schema.ArraySchema
import org.everit.json.schema.NumberSchema

class SchemaControlCreationTest {
	private val rootSchema: ObjectSchema = SchemaLoader.builder().useDefaults(true)
		.schemaJson(JSONObject(JSONTokener(this::class.java.getClassLoader().getResourceAsStream("StringSchema.json"))))
		.build().load().build() as ObjectSchema

	@Test
	fun verifyRootSchema() {
		assert(rootSchema.getTitle() == "schema")
	}

	@Test
	fun verifyStringSchema() {
		val stringSchema = rootSchema.getPropertySchemas().get("string") as StringSchema
		println(stringSchema.getLocation());
		println(stringSchema.getDefaultValue())
		println(stringSchema.getUnprocessedProperties())
		println(stringSchema.definesProperty("type"))
	}

	@Test
	fun verifyArraySchema() {
		val arraySchema = rootSchema.getPropertySchemas().get("string list") as ArraySchema
		println(arraySchema.getAllItemSchema())
		println(arraySchema.getContainedItemSchema())
		println(arraySchema.getItemSchemas())
	}

	@Test
	fun verifyTupleSchema() {
		val arraySchema = rootSchema.getPropertySchemas().get("tuple") as ArraySchema
		println(arraySchema.getAllItemSchema())
		println(arraySchema.getSchemaLocation())
		println(arraySchema.getContainedItemSchema())
		println(arraySchema.getItemSchemas())
		println(arraySchema.getItemSchemas().first().getSchemaLocation())
	}

	@Test
	fun verifyNumberSchema() {
		val schema = rootSchema.getPropertySchemas().get("number") as NumberSchema
		println(schema.getDefaultValue())
		println(schema.getExclusiveMaximumLimit())
		println(schema.getExclusiveMinimumLimit())
		println(schema.getMaximum())
		println(schema.getMinimum())
		println(schema.getMultipleOf())
	}

	@Test
	fun verifyIntegerSchema() {
		val schema = rootSchema.getPropertySchemas().get("integer") as NumberSchema
		println(schema.getDefaultValue())
		println(schema.getExclusiveMaximumLimit())
		println(schema.getExclusiveMinimumLimit())
		println(schema.getMaximum())
		println(schema.getMinimum())
		println(schema.getMultipleOf())
	}
}