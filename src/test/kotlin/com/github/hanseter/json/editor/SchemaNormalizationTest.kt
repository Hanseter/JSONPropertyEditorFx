package com.github.hanseter.json.editor

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
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
    fun mergesPartialRef() {
        val schema = JSONObject("""
{
  "definitions": {
    "foo": {
      "type": "string",
      "title": "ref title",
      "description": "ref desc"
    }
  },
  "properties": {
    "all": {
      "${'$'}ref": "#/definitions/foo"
    },
    "less": {
      "${'$'}ref": "#/definitions/foo",
      "title": "real title"
    },
    "least": {
      "${'$'}ref": "#/definitions/foo",
      "title": "real title",
      "description": "real desc"
    }
  }
}
        """)

        val result = SchemaNormalizer.resolveRefs(schema, null)
        val properties = result.getJSONObject("properties")

        assertThat(properties.keySet(), containsInAnyOrder("all", "less", "least"))

        assertThat(properties.getJSONObject("all").getString("type"), `is`("string"))
        assertThat(properties.getJSONObject("all").getString("title"), `is`("ref title"))
        assertThat(properties.getJSONObject("all").getString("description"), `is`("ref desc"))

        assertThat(properties.getJSONObject("less").getString("type"), `is`("string"))
        assertThat(properties.getJSONObject("less").getString("title"), `is`("real title"))
        assertThat(properties.getJSONObject("less").getString("description"), `is`("ref desc"))

        assertThat(properties.getJSONObject("least").getString("type"), `is`("string"))
        assertThat(properties.getJSONObject("least").getString("title"), `is`("real title"))
        assertThat(properties.getJSONObject("least").getString("description"), `is`("real desc"))
    }

    @Test
    fun normalizeDeeplyNestedSchema() {
        val schema = javaClass.classLoader.getResourceAsStream("nestedCompositeSchema.json").use {
            JSONObject(JSONTokener(it))
        }
        val result = SchemaNormalizer.normalizeSchema(schema, javaClass.classLoader.getResource("").toURI())
        assertThat(result.toString(1), `is`(JSONObject("""{
 "${'$'}schema": "http://json-schema.org/draft-07/schema",
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
""").toString(1))
        )
    }

    @Test
    fun correctlyMergesOrderWhenArray() {
        val schema = JSONObject(
                """{"allOf": [ 
    {
      "type":"object", 
      "properties": {"string0":{"type":"string"},"int0": {"type": "integer"}},
      "order": ["int0","string0"]
    },
    {
      "type":"object",
      "properties": {"string1":{"type":"string"},"int1": {"type": "integer"}},
      "order": ["string1","int1"]
    }
    ]}"""
        )
        val result = SchemaNormalizer.convertOrder(SchemaNormalizer.inlineCompositions(schema))
        assertThat(result.getJSONArray("order").toList(), contains("int0", "string0", "string1", "int1"))
    }

    @Test
    fun correctlyMergesOrderWhenMixed() {
        val schema = JSONObject(
                """{"allOf": [ 
    {
      "type":"object", 
      "properties": {"string0":{"type":"string"},"int0": {"type": "integer"}},
      "order": ["int0","string0"]
    },
    {
      "type":"object",
      "properties": {"string1":{"type":"string"},"int1": {"type": "integer"}},
      "order": {"string1": 4000,"int1": 800}
    }
    ]}"""
        )
        val result = SchemaNormalizer.convertOrder(SchemaNormalizer.inlineCompositions(schema))
        assertThat(result.getJSONArray("order").toList(), contains("int0", "string0", "int1", "string1"))
    }

    @Test
    fun correctlyMergesOrderWhenObject() {
        val schema = JSONObject(
                """{"allOf": [ 
    {
      "type":"object", 
      "properties": {"string0":{"type":"string"},"int0": {"type": "integer"}},
      "order": {"int0": 1,"string0": 7}
    },
    {
      "type":"object",
      "properties": {"string1":{"type":"string"},"int1": {"type": "integer"}},
      "order": {"string1": 4,"int1": 800}
    }
    ]}""")
        val result = SchemaNormalizer.convertOrder(SchemaNormalizer.inlineCompositions(schema))
        assertThat(result.getJSONArray("order").toList(), contains("int0", "string1", "string0", "int1"))
    }

    @Test
    fun ensureCorrectOrderFormat() {
        val schema = JSONObject(
                """{
      "type":"object", 
      "properties": {"obj0":{
      "type":"object",
      "properties": {"string1":{"type":"string"},"int1": {"type": "integer"}},
      "order": {"string1": 4000,"int1": 800}
      },"int0": {"type": "integer"}},
      "order": {"int0": 1,"obj0": 7}
    }""")

        val result = SchemaNormalizer.convertOrder(schema)
        assertThat(result.getJSONArray("order").toList(), contains("int0", "obj0"))
        assertThat(result.getJSONObject("properties").getJSONObject("obj0")
                .getJSONArray("order").toList(), contains("int1", "string1"))
    }

    @Test
    fun testRefToExternalFile() {
        val uri = this::class.java.classLoader.getResource("")?.toURI()

        val schema = JSONObject(
                """
{
  "type": "object",
  "properties": {
    "foo": {
      "${'$'}ref": "ReferencedPointSchema.json"
    }
  }
}
""")

        val result = SchemaNormalizer.resolveRefs(schema, uri)

        assertThat(result.query("#/properties/foo/type"), `is`("object"))
    }

    @Test
    fun testRefInsideDefinitions() {
        val schema = JSONObject(
                """
{
  "definitions": {
    "a": {
      "type": "integer"
    },
    "b": {
      "${'$'}ref": "#/definitions/a"
    },
    "c": {
      "type": "object",
      "properties": {
        "a": {
          "${'$'}ref": "#/definitions/b"
        }
      }
    }
  },
  "type": "object",
  "properties": {
    "foo": {
      "${'$'}ref": "#/definitions/b"
    },
    "bar": {
      "${'$'}ref": "#/definitions/c"
    }
  }
}
""")
        val result = SchemaNormalizer.resolveRefs(schema, null)

        assertThat(result.query("#/properties/foo/type"), `is`("integer"))

        assertThat(result.query("#/properties/bar/properties/a/type"), `is`("integer"))
    }

    @Test
    fun refInAdditionalProperties() {
        val uri = this::class.java.classLoader.getResource("")?.toURI()
        val schema = JSONObject(
                """
{
  "type": "object",
  "properties": {
    "foo": {
      "type": "object",
      "additionalProperties": {
        "${'$'}ref": "ReferencedPointSchema.json"
      }
    }
  }
}
""")
        val result = SchemaNormalizer.resolveRefs(schema, uri)

        assertThat(result.query("#/properties/foo/additionalProperties/type"), `is`("object"))
    }
}