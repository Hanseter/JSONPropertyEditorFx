package com.github.hanseter.json.editor

import javafx.scene.control.TextField
import javafx.stage.Stage
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start

@ExtendWith(ApplicationExtension::class)
class ReferenceSchemaTest {

    lateinit var editor: JsonPropertiesEditor

    @Start
    fun start(stage: Stage) {
        editor = JsonPropertiesEditor()
    }

    @Test
    fun displayExistingValue() {
        val schema =
                JSONObject("""{"definitions": {"test": {"type":"string"}},
                "type":"object","properties":{"string":{"${'$'}ref": "#/definitions/test"}}}""")
        editor.display("1", "1", JSONObject().put("string", "foobar"), schema) { it }
        val itemTable = editor.getItemTable()
        val stringControl = editor.getControlInTable("string") as TextField
        MatcherAssert.assertThat(stringControl.text, Matchers.`is`("foobar"))
    }
}