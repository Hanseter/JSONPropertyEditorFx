package com.github.hanseter.json.editor

import javafx.scene.control.TextField
import javafx.stage.Stage
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start
import org.testfx.util.WaitForAsyncUtils
import java.net.URI

@ExtendWith(ApplicationExtension::class)
class SchemaChangeTest {
    lateinit var editor: JsonPropertiesEditor

    @Start
    fun start(stage: Stage) {
        editor = JsonPropertiesEditor()
    }

    @Test
    fun schemaChangeOnEdit() {
        val schema = JSONObject(
            """
{
  "type": "object",
  "properties": {
    "src": {
      "type": "string"
    }
  }
}
        """
        )

        editor.display("1", "1", JSONObject(), schema) { it: PropertiesEditInput ->

            val newSchema = JSONObject(
                """
{
  "type": "object",
  "properties": {
    "src": {
      "type": "string"
    }
  }
}
            """
            )

            newSchema.getJSONObject("properties")
                .put(it.data.optString("src", "target"), JSONObject(mapOf("type" to "string")))



            PropertiesEditResult(it.data, ParsedSchema.create(newSchema, null as URI))
        }


        val srcControl = editor.getControlInTable("src") as TextField

        assertThat(editor.getItemTable().root.findChildWithKeyRecursive("src"), notNullValue())
        assertThat(editor.getItemTable().root.findChildWithKeyRecursive("hello"), nullValue())

        srcControl.text = "hello"
        WaitForAsyncUtils.waitForFxEvents()

        assertThat(editor.getItemTable().root.findChildWithKeyRecursive("hello"), notNullValue())

        srcControl.text = "goodbye"
        WaitForAsyncUtils.waitForFxEvents()

        assertThat(editor.getItemTable().root.findChildWithKeyRecursive("hello"), nullValue())
        assertThat(editor.getItemTable().root.findChildWithKeyRecursive("goodbye"), notNullValue())
    }
}