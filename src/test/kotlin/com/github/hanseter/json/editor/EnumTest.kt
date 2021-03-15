package com.github.hanseter.json.editor

import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.stage.Stage
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start

@ExtendWith(ApplicationExtension::class)
class EnumTest {

    lateinit var editor: JsonPropertiesEditor

    @Start
    fun start(stage: Stage) {
        editor = JsonPropertiesEditor()
    }

    @Test
    fun optionalEnumHasResetToDefault() {
        val schema = JSONObject("""
{
  "type":"object",
  "properties": {
    "e": {
      "type":"string",
      "enum": ["foo", "bar"]
    }
  }
}""")
        editor.display("1", "1", JSONObject(), schema) { JsonEditorData(it) }
        val itemTable = editor.getItemTable()
        val enumEntry = itemTable.root.children[0].findChildWithKey("e")!!
        assertThat(enumEntry.value.createActions()!!.children, Matchers.hasSize(1))
        assertThat((enumEntry.value.createActions()!!.children[0] as Button).text, Matchers.`is`("↻"))
    }

    @Test
    fun optionalDefaultEnumHasResetToDefault() {
        val schema = JSONObject("""
{
  "type":"object",
  "properties": {
    "e": {
      "type":"string",
      "enum": ["foo", "bar"],
      "default": "bar"
    }
  }
}""")
        editor.display("1", "1", JSONObject(), schema) { JsonEditorData(it) }
        val itemTable = editor.getItemTable()
        val enumEntry = itemTable.root.children[0].findChildWithKey("e")!!
        assertThat(enumEntry.value.createActions()!!.children, Matchers.hasSize(1))
        assertThat((enumEntry.value.createActions()!!.children[0] as Button).text, Matchers.`is`("↻"))
    }

    @Test
    fun requiredEnumHasNoResetToDefault() {
        val schema = JSONObject("""
{
  "type":"object",
  "properties": {
    "e": {
      "type":"string",
      "enum": ["foo", "bar"]
    }
  },
  "required": ["e"]
}""")
        editor.display("1", "1", JSONObject(), schema) { JsonEditorData(it) }
        val itemTable = editor.getItemTable()
        val enumEntry = itemTable.root.children[0].findChildWithKey("e")!!

        assertThat(enumEntry.value.createActions()!!.children, Matchers.emptyIterable())
    }

    @Test
    fun nullableEnumHasAllActions() {
        val schema = JSONObject("""
{
  "type":"object",
  "properties": {
    "e": {
      "type": ["string", "null"],
      "enum": ["foo", "bar", null],
      "default": "bar"
    }
  }
}""")
        editor.display("1", "1", JSONObject(), schema) { JsonEditorData(it) }
        val itemTable = editor.getItemTable()
        val enumEntry = itemTable.root.children[0].findChildWithKey("e")!!

        assertThat(enumEntry.value.createActions()!!.children, Matchers.hasSize(2))
        assertThat((enumEntry.value.createActions()!!.children[0] as Button).text, Matchers.`is`("↻"))
        assertThat((enumEntry.value.createActions()!!.children[1] as Button).text, Matchers.`is`("Ø"))
    }

    @Test
    fun nullableEnumHasCorrectComboBox() {
        val schema = JSONObject("""
{
  "type":"object",
  "properties": {
    "e": {
      "enum": ["foo", "bar", null]
    }
  }
}""")
        editor.display("1", "1", JSONObject(), schema) { JsonEditorData(it) }
        val itemTable = editor.getItemTable()
        val enumEntry = itemTable.root.children[0].findChildWithKey("e")!!

        val control = enumEntry.value.createControl()!!.control as ComboBox<*>

        assertThat(control.items, Matchers.containsInAnyOrder("foo", "bar"))
    }

    @Test
    fun nullableEnumButtonsWork() {
        val schema = JSONObject("""
{
  "type":"object",
  "properties": {
    "e": {
      "type": ["string", "null"],
      "enum": ["foo", "bar", null],
      "default": "bar"
    }
  }
}""")
        val data = JSONObject("""{"e": "foo"}""")

        editor.display("1", "1", data, schema) { JsonEditorData(it) }
        val itemTable = editor.getItemTable()
        val enumEntry = itemTable.root.children[0].findChildWithKey("e")!!

        val control = enumEntry.value.createControl()
        val comboBox = control!!.control as ComboBox<*>

        val defaultButton = enumEntry.value.createActions()!!.children[0] as Button
        val nullButton = enumEntry.value.createActions()!!.children[1] as Button

        defaultButton.fire()

        control.updateDisplayedValue()

        assertThat(data.has("e"), Matchers.`is`(false))
        assertThat(comboBox.value, Matchers.`is`("bar"))
        assertThat(comboBox.styleClass, Matchers.hasItem("has-default-value"))

        nullButton.fire()

        control.updateDisplayedValue()

        assertThat(data.opt("e"), Matchers.`is`(JSONObject.NULL))
        assertThat(comboBox.value, Matchers.nullValue())
    }

}