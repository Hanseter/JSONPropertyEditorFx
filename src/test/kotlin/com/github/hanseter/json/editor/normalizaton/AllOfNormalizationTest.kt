package com.github.hanseter.json.editor.normalizaton

import com.github.hanseter.json.editor.SchemaNormalizer
import com.github.hanseter.json.editor.base.SimilarObjectMatcher
import org.hamcrest.MatcherAssert
import org.json.JSONObject
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

class AllOfNormalizationTest {

    private fun generateTests(inputSchemas: List<JSONObject>, mergedSchema: JSONObject): List<DynamicTest> {
        val inputSchemaCombinations = inputSchemas.fold(emptyList()) { acc: List<List<Pair<JSONObject, Boolean>>>, current ->
            if (acc.isEmpty()) {
                listOf(listOf(current to true), listOf(current to false))
            } else acc.flatMap { listOf(it + (current to true), it + (current to false)) }
        }

        return inputSchemaCombinations
            .map { schemas -> DynamicTest.dynamicTest(
                schemas.withIndex().joinToString {
                    "Schema #${it.index} ${if (it.value.second) "${'$'}ref" else "inline"}"
                }
            ) {
                val input = JSONObject()
                    .put("type", "object")

                val definitions = JSONObject().also { input.put("definitions", it) }

                for (schema in schemas.withIndex().filter { it.value.second }) {
                    definitions.put("schema${schema.index}", schema.value.first)
                }

                input.put("properties", JSONObject().put("sub", JSONObject().put("allOf", schemas.withIndex().map {
                    if (it.value.second) {
                        JSONObject().put("${'$'}ref", "#/definitions/schema${it.index}")
                    } else {
                        it.value.first
                    }
                })))

                val normalized = SchemaNormalizer.normalizeSchema(input, null)

                val expected = JSONObject()
                    .put("type", "object")
                    .put("definitions", definitions)
                    .put("properties", JSONObject().put("sub", mergedSchema))

                MatcherAssert.assertThat(normalized, SimilarObjectMatcher(expected))
            } }
    }

    @TestFactory
    fun `key overriding`(): List<DynamicTest> {
        return generateTests(
            listOf(
                stringPropWithDate(),
                stringPropWithDateTime()
            ),
            stringPropWithDateTime()
        )
    }

    @TestFactory
    fun `property merging`(): List<DynamicTest> {
        return generateTests(
            listOf(
                JSONObject()
                    .put("type", "object")
                    .put("properties", JSONObject()
                        .put("foo", JSONObject().put("type", "boolean"))),
                JSONObject()
                    .put("type", "object")
                    .put("properties", JSONObject()
                        .put("bar", JSONObject().put("type", "number"))),
                JSONObject()
                    .put("type", "object")
                    .put("properties", JSONObject()
                        .put("baz", JSONObject().put("type", "integer").put("title", "Baz")))
            ),
            JSONObject()
                .put("type", "object")
                .put("properties", JSONObject()
                    .put("foo", JSONObject().put("type", "boolean"))
                    .put("bar", JSONObject().put("type", "number"))
                    .put("baz", JSONObject().put("type", "integer").put("title", "Baz"))
                )
        )
    }

    @TestFactory
    fun `order merging`(): List<DynamicTest> {
        return generateTests(
            listOf(
                JSONObject()
                    .put("type", "object")
                    .put("properties", JSONObject()
                        .put("foo", JSONObject().put("type", "boolean"))
                        .put("foo2", JSONObject().put("type", "boolean"))
                    )
                    .put("order", listOf("foo2", "foo")),
                JSONObject()
                    .put("type", "object")
                    .put("properties", JSONObject()
                        .put("bar", JSONObject().put("type", "number"))
                        .put("bar2", JSONObject().put("type", "number"))
                    )
                    .put("order", mapOf("bar" to 1000)),
                JSONObject()
                    .put("type", "object")
                    .put("properties", JSONObject()
                        .put("baz", JSONObject().put("type", "integer").put("title", "Baz"))
                        .put("baz2", JSONObject().put("type", "integer"))
                    )
                    .put("order", mapOf("baz" to 10, "baz2" to 20))
            ),
            JSONObject()
                .put("type", "object")
                .put("properties", JSONObject()
                    .put("foo", JSONObject().put("type", "boolean"))
                    .put("foo2", JSONObject().put("type", "boolean"))
                    .put("bar", JSONObject().put("type", "number"))
                    .put("bar2", JSONObject().put("type", "number"))
                    .put("baz", JSONObject().put("type", "integer").put("title", "Baz"))
                    .put("baz2", JSONObject().put("type", "integer"))
                )
                .put("order", listOf("foo2", "foo", "baz", "baz2", "bar"))
        )
    }

    @TestFactory
    fun `required merging`(): List<DynamicTest> {
        return generateTests(
            listOf(
                JSONObject()
                    .put("type", "object")
                    .put("properties", JSONObject()
                        .put("foo", JSONObject().put("type", "boolean"))
                        .put("foo2", JSONObject().put("type", "boolean"))
                    )
                    .put("required", listOf("foo2")),
                JSONObject()
                    .put("type", "object")
                    .put("properties", JSONObject()
                        .put("bar", JSONObject().put("type", "number"))
                        .put("bar2", JSONObject().put("type", "number"))
                    )
                    .put("required", listOf("bar")),
                JSONObject()
                    .put("type", "object")
                    .put("properties", JSONObject()
                        .put("baz", JSONObject().put("type", "integer").put("title", "Baz"))
                        .put("baz2", JSONObject().put("type", "integer"))
                    )
            ),
            JSONObject()
                .put("type", "object")
                .put("properties", JSONObject()
                    .put("foo", JSONObject().put("type", "boolean"))
                    .put("foo2", JSONObject().put("type", "boolean"))
                    .put("bar", JSONObject().put("type", "number"))
                    .put("bar2", JSONObject().put("type", "number"))
                    .put("baz", JSONObject().put("type", "integer").put("title", "Baz"))
                    .put("baz2", JSONObject().put("type", "integer"))
                )
                .put("required", listOf("foo2", "bar"))
        )
    }

    companion object {

        val stringPropWithDate = {
            JSONObject()
                .put("type", "string")
                .put("format", "date")
        }

        val stringPropWithDateTime = {
            JSONObject()
                .put("type", "string")
                .put("format", "date-time")
        }

    }

}