package com.github.hanseter.json.editor

import javafx.scene.control.TextField
import javafx.stage.Stage
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start
import org.testfx.util.WaitForAsyncUtils

@ExtendWith(ApplicationExtension::class)
class ValidationKeywordsTest {

    private lateinit var editor: JsonPropertiesEditor

    @Start
    fun start(stage: Stage) {
        editor = JsonPropertiesEditor()
    }

    @Test
    fun testNot() {
        val schema = JSONObject("""
{
  "properties": {
    "a": {
      "type": "string",
      "not": {
        "const": "bar"
      }
    }
  }
}
        """)

        editor.display("1", "1", JSONObject().put("a", "foo"), schema) { it }

        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(true))

        val stringControl = editor.getControlInTable("a") as TextField

        stringControl.text = "bar"

        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(false))
    }

    @Test
    fun testConditional() {
        val schema = JSONObject("""
{
  "type":"object",
  "properties": {
    "nested": {
        "type":"object",
        "properties": {
          "a": {
            "type": "string"
          },
          "b": {
            "type": "string"
          }
        },
        "if": {
          "properties": {
            "a": {
              "const": "foo"
            }
          }
        },
        "then": {
          "properties": {
            "b": {
              "pattern": ".*bar.*"
            }
          }
        },
        "else": {
          "properties": {
            "b": {
              "pattern": ".*baz.*"
            }
          }
        }
     }
  }
}
        """)

        editor.display("1", "1", JSONObject("""{"nested":{"a": "foo", "b": "not"}}"""), schema) { it }

        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(false))

        val itemTable = editor.getItemTable()
        val aControl = editor.getControlInTable("a") as TextField
        val bControl = editor.getControlInTable("b") as TextField

        bControl.text = "something bar something"
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(true))


        aControl.text = "bar"
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(false))

        bControl.text = "something baz something"
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(true))
    }
}