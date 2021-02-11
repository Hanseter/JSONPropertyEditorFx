package com.github.hanseter.json.editor

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.containsInAnyOrder
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import org.junit.jupiter.api.Test

class SchemaNormalizationTest {

    @Test
    fun resolveSimpleRef() {
        val schema =
            JSONObject(
                """{"definitions": {"test": {"type":"string"}},
                "type":"object","properties":{"string":{"${'$'}ref": "#/definitions/test"}}}"""
            )
        val result = SchemaNormalizer.resolveRefs(schema, null)
        assertThat(
            result.getJSONObject("properties").getJSONObject("string").getString("type"),
            `is`("string")
        )
    }

    @Test
    fun resolveRootRef() {
        val objSchema =
            JSONObject("""{"type":"object","properties":{"string":{"type":"string"}}}""")
        val schema = JSONObject().put("definitions", JSONArray().put(objSchema))
            .put("${'$'}ref", "#/definitions/0")
        val result = SchemaNormalizer.resolveRefs(schema, null)
        assertThat(
            result.getJSONObject("properties").getJSONObject("string").getString("type"),
            `is`("string")
        )
    }

    @Test
    fun resolveAllOfRef() {
        val schema =
            JSONObject(
                """{"definitions": [ 
                    {
                      "type":"object","properties": {"string0":{"type":"string"}}
                    },
                    {
                      "type":"object", "properties": {"string1":{"type":"string"}}
                    }
                    ],
                "allOf":[
                {"${'$'}ref": "#/definitions/0"}, {"${'$'}ref": "#/definitions/1"}
                ]}"""
            )

        val result = SchemaNormalizer.resolveRefs(schema, null)
        println(schema.toString(1))
        assertThat(
            result.getJSONArray("allOf").getJSONObject(0).getJSONObject("properties")
                .getJSONObject("string0").getString("type"),
            `is`("string")
        )
        assertThat(
            result.getJSONArray("allOf").getJSONObject(1).getJSONObject("properties")
                .getJSONObject("string1").getString("type"),
            `is`("string")
        )
    }

    @Test
    fun mergesAllOfs() {
        val schema = JSONObject(
            """{"allOf": [ 
                    {
                      "type":"object", 
                      "properties": {"string0":{"type":"string"},"int0": {"type": "integer"}},
                      "required": ["int0"]
                    },
                    {
                      "type":"object",
                      "properties": {"string1":{"type":"string"},"int1": {"type": "integer"}},
                      "required": ["string1"]
                    }
                    ]}"""
        )
//        val expected = JSONObject(
//            """{
//           "type":"object",
//           "properties": {
//            "string0":{"type":"string"},
//            "string1":{"type":"string"},
//            "int0":{"type":"string"},
//            "int1":{"type":"string"}
//           },
//           "required": [
//           "int0","string1"
//           ]
//        }"""
//        )
        val result = SchemaNormalizer.inlineCompositions(schema)
        assertThat(result.getString("type"), `is`("object"))
        val properties = result.getJSONObject("properties")
        assertThat(properties.keySet(), containsInAnyOrder("string0", "string1", "int0", "int1"))
        assertThat(properties.getJSONObject("string0").getString("type"), `is`("string"))
        assertThat(properties.getJSONObject("string1").getString("type"), `is`("string"))
        assertThat(properties.getJSONObject("int0").getString("type"), `is`("integer"))
        assertThat(properties.getJSONObject("int1").getString("type"), `is`("integer"))
    }

    @Test
    fun normalizeDeeplyNestedSchema() {
        val schema = javaClass.classLoader.getResourceAsStream("nestedCompositeSchema.json").use {
            JSONObject(JSONTokener(it))
        }
        val result = SchemaNormalizer.normalizeSchema(schema, javaClass.classLoader.getResource("").toURI())
        assertThat(result.toString(), equals(JSONObject("""{
 "$schema": "http://json-schema.org/draft-07/schema",
 "title": "composite",
 "type": "object",
 "properties": {
  "fromNested": {"type": "string"},
  "nestedAllOf": {
   "type": "object",
   "properties": {
    "allOf1": {"type": "string"},
    "allOf2.2": {"type": "string"},
    "allOf2.1": {"type": "string"}
   }
  },
  "additional": {"type": "number"},
  "x": {"type": "number"},
  "name": {"type": "string"},
  "y": {"type": "number"}
 }
}
""").toString()))
    }
}