package com.github.hanseter.json.editor

import javafx.scene.control.ComboBox
import javafx.scene.control.Spinner
import javafx.scene.control.TextField
import javafx.stage.Stage
import org.everit.json.schema.NumberSchema
import org.everit.json.schema.Schema
import org.everit.json.schema.StringSchema
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start

@ExtendWith(ApplicationExtension::class)
class OneOfTest {
    lateinit var editor: JsonPropertiesEditor


    @Start
    fun start(stage: Stage) {
        editor = JsonPropertiesEditor()
    }

    @Test
    fun offersSelectionWhenKeyIsNull() {
        val schema = JSONObject("""{
"type":"object",
"properties":{
"choice": {
"oneOf":[
    {"type":"string"},
    {"type":"number"}
]}}}""")
        editor.display("1", "1", JSONObject().put("choice", JSONObject.NULL), schema) { it }
        val itemTable = editor.getItemTable()
        val item = itemTable.root.children.first().findChildWithKey("choice")!!
        val control = item.value.createControl()?.control as ComboBox<Schema>
        assertThat(control.items, containsInAnyOrder(instanceOf(StringSchema::class.java), instanceOf(NumberSchema::class.java)))
        assertThat(control.selectionModel.selectedItem, `is`(nullValue()))
        val displayedNames = control.items.map { control.converter.toString(it) }
        assertThat(displayedNames, contains("0", "1"))
        assertThat(item.children, `is`(empty()))
    }

    @Test
    fun offersSelectionWhenKeyIsMissing() {
        val schema = JSONObject("""{
"type":"object",
"properties":{
"choice": {
"oneOf":[
    {"type":"string"},
    {"type":"number"}
]}}}""")
        editor.display("1", "1", JSONObject(), schema) { it }
        val itemTable = editor.getItemTable()
        val item = itemTable.root.children.first().findChildWithKey("choice")!!
        val control = item.value.createControl()?.control as ComboBox<Schema>
        assertThat(control.items, containsInAnyOrder(instanceOf(StringSchema::class.java), instanceOf(NumberSchema::class.java)))
        assertThat(control.selectionModel.selectedItem, `is`(nullValue()))
        val displayedNames = control.items.map { control.converter.toString(it) }
        assertThat(displayedNames, contains("0", "1"))
        assertThat(item.children, `is`(empty()))
    }

    @Test
    fun selectAndEdit() {
        val schema = JSONObject("""{
"type":"object",
"properties":{
"choice": {
"oneOf":[
    {"type":"string"},
    {"type":"number"}
]}}}""")
        val data = JSONObject()
        editor.display("1", "1", data, schema) { it }
        val itemTable = editor.getItemTable()
        val item = itemTable.root.children.first().findChildWithKey("choice")!!
        val control = item.value.createControl()?.control as ComboBox<Schema>
        control.selectionModel.selectFirst()
        val stringItem = item.children.single()
        val stringControl = stringItem.value.createControl()?.control as TextField
        stringControl.text = "Foobar"
        assertThat(data.keySet(), contains("choice"))
        assertThat(data.getString("choice"), `is`("Foobar"))
    }

    @Test
    fun displaysTitleInChoice() {
        val schema = JSONObject("""{
"type":"object",
"properties":{
"choice": {
"oneOf":[
    {"type":"string", "title": "B"},
    {"type":"number", "title": "A"}
]}}}""")
        editor.display("1", "1", JSONObject(), schema) { it }
        val itemTable = editor.getItemTable()
        val item = itemTable.root.children.first().findChildWithKey("choice")!!
        val control = item.value.createControl()?.control as ComboBox<Schema>
        assertThat(control.items, containsInAnyOrder(instanceOf(StringSchema::class.java), instanceOf(NumberSchema::class.java)))
        assertThat(control.selectionModel.selectedItem, `is`(nullValue()))
        val displayedNames = control.items.map { control.converter.toString(it) }
        assertThat(displayedNames, contains("B", "A"))
        assertThat(item.children, `is`(empty()))
    }

    @Test
    fun displaysCorrectOneOfWhenComplexDataIsPresent() {
        val schema = JSONObject("""{
"type":"object",
"properties":{
"choice": {
"oneOf":[
    {"type":"object",
      "properties": {"a":{"type": "string"}, "b": {"type": "number"}}},
    {"type":"object",
      "properties": {"a":{"type": "number"}, "b": {"type": "number"}}}
]}}}""")
        editor.display("1", "1", JSONObject().put("choice", JSONObject().put("a", "78").put("b", 6)), schema) { it }
        val control = editor.getControlInTable("choice") as ComboBox<Schema>
        assertThat(control.selectionModel.selectedIndex, `is`(0))
    }

    @Test
    fun displaysCorrectOneOfWhenSimpleDataIsPresent() {
        val schema = JSONObject("""{
"type":"object",
"properties":{
"choice": {
"oneOf":[
    {"type":"string"},
    {"type":"number"}
]}}}""")
        val data = JSONObject().put("choice", 15)
        editor.display("1", "1", data, schema) { it }
        val itemTable = editor.getItemTable()
        val item = itemTable.root.children.first().findChildWithKey("choice")!!
        val control = editor.getControlInTable(item.value) as ComboBox<Schema>

        assertThat(control.selectionModel.selectedItem, `is`(instanceOf(NumberSchema::class.java)))
        val spinner = editor.getControlInTable(item.children.single().value) as Spinner<Number>
        spinner.increment(1)
        assertThat(data.keySet(), containsInAnyOrder("choice"))
        assertThat(data.getInt("choice"), `is`(16))
    }
}