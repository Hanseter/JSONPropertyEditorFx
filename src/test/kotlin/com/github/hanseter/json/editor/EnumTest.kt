package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.ui.TreeItemData
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TreeItem
import javafx.scene.layout.StackPane
import javafx.scene.text.Text
import javafx.stage.Stage
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
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
        val schema = JSONObject(
            """
{
  "type":"object",
  "properties": {
    "e": {
      "type":"string",
      "enum": ["foo", "bar"]
    }
  }
}"""
        )
        editor.display("1", "1", JSONObject(), schema) { it }
        val itemTable = editor.getItemTable()
        val enumEntry = itemTable.root.children[0].findChildWithKey("e")!!
        assertThat(enumEntry.value.createActions()!!.children, hasSize(1))
        assertThat(getActionIconAsText(enumEntry, 0), `is`("↻"))
    }

    @Test
    fun optionalDefaultEnumHasResetToDefault() {
        val schema = JSONObject(
            """
{
  "type":"object",
  "properties": {
    "e": {
      "type":"string",
      "enum": ["foo", "bar"],
      "default": "bar"
    }
  }
}"""
        )
        editor.display("1", "1", JSONObject(), schema) { it }
        val itemTable = editor.getItemTable()
        val enumEntry = itemTable.root.children[0].findChildWithKey("e")!!
        assertThat(enumEntry.value.createActions()!!.children, hasSize(1))
        assertThat(getActionIconAsText(enumEntry, 0), `is`("↻"))
    }

    @Test
    fun requiredEnumHasNoResetToDefault() {
        val schema = JSONObject(
            """
{
  "type":"object",
  "properties": {
    "e": {
      "type":"string",
      "enum": ["foo", "bar"]
    }
  },
  "required": ["e"]
}"""
        )
        editor.display("1", "1", JSONObject(), schema) { it }
        val itemTable = editor.getItemTable()
        val enumEntry = itemTable.root.children[0].findChildWithKey("e")!!

        assertThat(enumEntry.value.createActions()!!.children, emptyIterable())
    }

    @Test
    fun nullableEnumHasAllActions() {
        val schema = JSONObject(
            """
{
  "type":"object",
  "properties": {
    "e": {
      "type": ["string", "null"],
      "enum": ["foo", "bar", null],
      "default": "bar"
    }
  }
}"""
        )
        editor.display("1", "1", JSONObject(), schema) { it }
        val itemTable = editor.getItemTable()
        val enumEntry = itemTable.root.children[0].findChildWithKey("e")!!

        assertThat(enumEntry.value.createActions()!!.children, hasSize(2))
        assertThat(getActionIconAsText(enumEntry, 0), `is`("Ø"))
        assertThat(getActionIconAsText(enumEntry, 1), `is`("↻"))
    }

    @Test
    fun nullableEnumHasCorrectComboBox() {
        val schema = JSONObject(
            """
{
  "type":"object",
  "properties": {
    "e": {
      "enum": ["foo", "bar", null]
    }
  }
}"""
        )
        editor.display("1", "1", JSONObject(), schema) { it }
        val itemTable = editor.getItemTable()
        val enumEntry = itemTable.root.children[0].findChildWithKey("e")!!

        val control = enumEntry.value.createControl()!!.control as ComboBox<*>

        assertThat(control.items, containsInAnyOrder("foo", "bar"))
    }

    @Test
    fun nullableEnumButtonsWork() {
        val schema = JSONObject(
            """
{
  "type":"object",
  "properties": {
    "e": {
      "type": ["string", "null"],
      "enum": ["foo", "bar", null],
      "default": "bar"
    }
  }
}"""
        )
        val data = JSONObject("""{"e": "foo"}""")

        editor.display("1", "1", data, schema) { it }
        val itemTable = editor.getItemTable()
        val enumEntry = itemTable.root.children[0].findChildWithKey("e")!!

        val control = enumEntry.value.createControl()
        val comboBox = control!!.control as ComboBox<*>

        val defaultButton = enumEntry.value.createActions()!!.children[1] as Button
        val nullButton = enumEntry.value.createActions()!!.children[0] as Button

        defaultButton.fire()

        control.updateDisplayedValue()

        assertThat(data.has("e"), `is`(false))
        assertThat(comboBox.value, `is`("bar"))
        assertThat(comboBox.styleClass, hasItem("has-default-value"))

        nullButton.fire()

        control.updateDisplayedValue()

        assertThat(data.opt("e"), `is`(JSONObject.NULL))
        assertThat(comboBox.value, nullValue())
    }

    companion object {
        fun getActionIcon(item: TreeItem<TreeItemData>, index: Int): Node {
            return ((item.value.createActions()!!.children[index] as Button).graphic as StackPane).children.single()
        }

        fun getActionIconAsText(item: TreeItem<TreeItemData>, index: Int): String {
            return ((getActionIcon(item, index) as Group).children.single() as Text).text
        }
    }
}