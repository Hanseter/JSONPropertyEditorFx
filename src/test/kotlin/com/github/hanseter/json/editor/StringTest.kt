package com.github.hanseter.json.editor

import javafx.scene.control.TextField
import javafx.stage.Stage
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start
import org.testfx.util.WaitForAsyncUtils

@ExtendWith(ApplicationExtension::class)
class StringTest {

    private lateinit var editor: JsonPropertiesEditor


    @Start
    fun start(stage: Stage) {
        editor = JsonPropertiesEditor()
    }

    @Test
    fun displayExistingValue() {
        val schema = JSONObject("""{"type":"object","properties":{"string":{"type":"string"}}}""")
        editor.display("1", "1", JSONObject().put("string", "foobar"), schema) { it }
        val itemTable = editor.getItemTable()
        val stringControl = editor.getControlInTable("string") as TextField
        assertThat(stringControl.text, `is`("foobar"))
    }

    @Test
    fun modifyValueByTextInput() {
        val schema = JSONObject("""{"type":"object","properties":{"string":{"type":"string"}}}""")
        val data = JSONObject().put("string", "foobar")
        editor.display("1", "1", data, schema) { it }
        val itemTable = editor.getItemTable()
        val stringControl = editor.getControlInTable("string") as TextField
        stringControl.text = "something"
        assertThat(data.getString("string"), `is`("something"))
    }

    @Test
    fun minLengthValidation() {
        val schema = JSONObject("""{"type":"object","properties":{"string":{"type":"string","minLength":15}}}""")
        val data = JSONObject().put("string", "foobar")
        editor.display("1", "1", data, schema) { it }
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(false))
    }

    @Test
    fun minLengthValidationAfterChange() {
        val schema = JSONObject("""{"type":"object","properties":{"string":{"type":"string","minLength":5}}}""")
        val data = JSONObject().put("string", "foobar")
        editor.display("1", "1", data, schema) { it }
        val stringControl = editor.getControlInTable("string") as TextField
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(true))
        stringControl.text = "bar"
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(false))
    }

    @Test
    fun maxLengthValidation() {
        val schema = JSONObject("""{"type":"object","properties":{"string":{"type":"string","maxLength":5}}}""")
        val data = JSONObject().put("string", "foobar")
        editor.display("1", "1", data, schema) { it }
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(false))
    }

    @Test
    fun maxLengthValidationAfterChange() {
        val schema = JSONObject("""{"type":"object","properties":{"string":{"type":"string","maxLength":15}}}""")
        val data = JSONObject().put("string", "foobar")
        editor.display("1", "1", data, schema) { it }
        val itemTable = editor.getItemTable()
        val stringControl = editor.getControlInTable("string") as TextField
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(true))
        stringControl.text = "longer than 15 chars"
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(false))
    }

    @Test
    fun patternValidation() {
        val schema = JSONObject("""{"type":"object","properties":{"string":{"type":"string","pattern":"^b.*"}}}""")
        val data = JSONObject().put("string", "foobar")
        editor.display("1", "1", data, schema) { it }
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(false))
    }

    @Test
    fun patternValidationAfterChange() {
        val schema = JSONObject("""{"type":"object","properties":{"string":{"type":"string","pattern":"f.*"}}}""")
        val data = JSONObject().put("string", "foobar")
        editor.display("1", "1", data, schema) { it }
        val itemTable = editor.getItemTable()
        val stringControl = editor.getControlInTable("string") as TextField
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(true))
        stringControl.text = "bar"
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(false))
    }

    @Test
    fun formatValidation() {
        val schema = JSONObject("""{"type":"object","properties":{"string":{"type":"string","format":"date"}}}""")
        val data = JSONObject().put("string", "foobar")
        editor.display("1", "1", data, schema) { it }
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(false))
    }

    @Test
    fun formatValidationAfterModification() {
        val schema = JSONObject("""{"type":"object","properties":{"string":{"type":"string","format":"date"}}}""")
        val data = JSONObject().put("string", "2018-11-13")
        editor.display("1", "1", data, schema) { it }
        val itemTable = editor.getItemTable()
        val stringControl = editor.getControlInTable("string") as TextField
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(true))
        stringControl.text = "bar"
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(false))
    }

    @Test
    fun multiValidation() {
        val schema = JSONObject("""{"type":"object","properties":{"string":{"type":"string","pattern":"foo.+","minLength":5}}}""")
        val data = JSONObject().put("string", "foobar")
        editor.display("1", "1", data, schema) { it }
        val itemTable = editor.getItemTable()
        val stringControl = editor.getControlInTable("string") as TextField
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(true))
        stringControl.text = "long enough but pattern does not match"
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(false))
        stringControl.text = "foobar long"
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(true))
        stringControl.text = "foo"
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(false))
    }

    @Test
    fun displayDefaultValue() {
        val schema = JSONObject("""{"type":"object","properties":{"string":{"type":"string","default": "foobar"}}}""")
        editor.display("1", "1", JSONObject(), schema) { it }
        val itemTable = editor.getItemTable()
        val stringControl = editor.getControlInTable("string") as TextField
        assertThat(stringControl.text, `is`("foobar"))
    }

    @Test
    fun displayNull() {
        val schema = JSONObject("""{"type":"object","properties":{"string":{"type":"string"}}}""")
        editor.display("1", "1", JSONObject(), schema) { it }
        val itemTable = editor.getItemTable()
        val stringControl = editor.getControlInTable("string") as TextField
        assertThat(stringControl.text, `is`(nullValue()))
    }
}