package com.github.hanseter.json.editor

import javafx.scene.control.Control
import javafx.scene.control.TextField
import org.controlsfx.control.ToggleSwitch
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.util.WaitForAsyncUtils

@ExtendWith(ApplicationExtension::class)
class PrimitivesNullDefaultTest {
    private class TestData(val type: String, val default: Any, val valueExtractor: (Control) -> Any?) {
        val property: JSONObject
            get() = JSONObject("""{"type":"$type"}""")
        val propertyWithDefault: JSONObject
            get() = property.put("default", default)
    }

    private val testData = listOf(
            TestData("boolean", true) { (it as ToggleSwitch).isSelected },
            TestData("string", "default") { (it as TextField).text }
    )

    fun getSchema(property: JSONObject): JSONObject = JSONObject("""{"type":"object","properties":{}}""").apply {
        getJSONObject("properties").put("key", property)
    }

    fun makeNullable(schema: JSONObject): JSONObject {
        val typeObj = schema.getJSONObject("properties").getJSONObject("key")
        val type = typeObj.getString("type")
        typeObj.put("type", listOf(type, "null"))
        return schema
    }

    fun getRequiredSchema(property: JSONObject): JSONObject = getSchema(property).put("required", JSONArray(listOf("key")))

    @Nested
    inner class IsNotInData {
        private val data = JSONObject()

        @Nested
        inner class HasDefault {
            @Test
            fun displaysDefaultIfRequired() {
                testData.forEach {
                    val editor = JsonPropertiesEditor()
                    editor.display("id", "title", data, getRequiredSchema(it.propertyWithDefault)) { it }
                    WaitForAsyncUtils.waitForFxEvents()
                    val control = editor.getControlInTable("key") as Control
                    assertThat(it.valueExtractor(control), `is`(it.default))
                    assertThat(control.styleClass.contains("has-default-value"), `is`(true))
                    assertThat(editor.valid.get(), `is`(true))
                }
            }

            @Test
            fun displaysDefaultIfOptional() {
                testData.forEach {
                    val editor = JsonPropertiesEditor()
                    editor.display("id", "title", data, getSchema(it.propertyWithDefault)) { it }
                    WaitForAsyncUtils.waitForFxEvents()
                    val control = editor.getControlInTable("key") as Control
                    assertThat(it.valueExtractor(control), `is`(it.default))
                    assertThat(control.styleClass.contains("has-default-value"), `is`(true))
                    assertThat(editor.valid.get(), `is`(true))
                }
            }
        }

        @Nested
        inner class HasNoDefault {
            @Test
            fun displaysNullAndErrorIfRequired() {
                testData.forEach {
                    val editor = JsonPropertiesEditor()
                    editor.display("id", "title", data, getRequiredSchema(it.property)) { it }
                    WaitForAsyncUtils.waitForFxEvents()
                    val control = editor.getControlInTable("key") as Control
                    assertThat(control.styleClass.contains("has-null-value"), `is`(true))
                    assertThat(editor.valid.get(), `is`(false))
                }
            }

            @Test
            fun displaysNullIfOptional() {
                testData.forEach {
                    val editor = JsonPropertiesEditor()
                    editor.display("id", "title", data, getSchema(it.property)) { it }
                    WaitForAsyncUtils.waitForFxEvents()
                    val control = editor.getControlInTable("key") as Control
                    assertThat(control.styleClass.contains("has-null-value"), `is`(true))
                    assertThat(editor.valid.get(), `is`(true))
                }
            }
        }
    }

    @Nested
    inner class IsExplicitlyNull {
        val data = JSONObject().put("key", JSONObject.NULL)

        @Nested
        inner class HasDefault {
            @Test
            fun displaysNullAndError() {
                testData.flatMap { listOf(getSchema(it.propertyWithDefault), getRequiredSchema(it.propertyWithDefault)) }.forEach { schema ->
                    val editor = JsonPropertiesEditor()
                    editor.display("id", "title", data, schema) { it }
                    WaitForAsyncUtils.waitForFxEvents()
                    val control = editor.getControlInTable("key") as Control
                    assertThat(control.styleClass.contains("has-null-value"), `is`(true))
                    assertThat(editor.valid.get(), `is`(false))
                }
            }

            @Test
            fun displaysNullIfNullable() {
                testData.flatMap { listOf(getSchema(it.propertyWithDefault), getRequiredSchema(it.propertyWithDefault)) }.map { makeNullable(it) }.forEach { schema ->
                    val editor = JsonPropertiesEditor()
                    editor.display("id", "title", data, schema) { it }
                    WaitForAsyncUtils.waitForFxEvents()
                    val control = editor.getControlInTable("key") as Control
                    assertThat(control.styleClass.contains("has-null-value"), `is`(true))
                    assertThat(editor.valid.get(), `is`(true))
                }
            }
        }

        @Nested
        inner class HasNoDefault {
            @Test
            fun displaysNullAndError() {
                testData.flatMap { listOf(getSchema(it.property), getRequiredSchema(it.property)) }.forEach { schema ->
                    val editor = JsonPropertiesEditor()
                    editor.display("id", "title", data, schema) { it }
                    WaitForAsyncUtils.waitForFxEvents()
                    val control = editor.getControlInTable("key") as Control
                    assertThat(control.styleClass.contains("has-null-value"), `is`(true))
                    assertThat(editor.valid.get(), `is`(false))
                }
            }

            @Test
            fun displaysNullIfNullable() {
                testData.flatMap { listOf(getSchema(it.property), getRequiredSchema(it.property)) }.map { makeNullable(it) }.forEach { schema ->
                    val editor = JsonPropertiesEditor()
                    editor.display("id", "title", data, schema) { it }
                    WaitForAsyncUtils.waitForFxEvents()
                    val control = editor.getControlInTable("key") as Control
                    assertThat(control.styleClass.contains("has-null-value"), `is`(true))
                    assertThat(editor.valid.get(), `is`(true))
                }
            }
        }
    }
}