package com.github.hanseter.json.editor

import javafx.scene.control.Button
import javafx.stage.Stage
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start

@ExtendWith(ApplicationExtension::class)
class ArrayTest {
    lateinit var editor: JsonPropertiesEditor

    @Start
    fun start(stage: Stage) {
        editor = JsonPropertiesEditor()
    }

    @Test
    fun readOnlyArrays() {
        val schema = JSONObject("""{"type":"object","properties":{"bar":{"type":"array",
            "readOnly":true,
            "items":{"type":"string"}
            }}}""")
        editor.display("1", "1", JSONObject(), schema) { it }
        val itemTable = editor.getItemTable()
        val arrayEntry = itemTable.root.children[0].findChildWithKey("bar")!!
        assertThat(arrayEntry.value.createActions()!!.children, `is`(empty()))
    }

    @Test
    fun nestedReadOnlyAndWritable() {
        val schema = JSONObject("""{"type":"object","properties":{"bar":{"type":"array",
            "readOnly":true,
            "items":{"type":"object", "properties":{"a":{"type":"number"},"b":{"type":"boolean", "readOnly":false}}}
            }}}""")
        editor.display("1", "1", JSONObject("""{"bar":[{"a":42,"b":true},{"a":32,"b":true}]}"""), schema) { it }
        val itemTable = editor.getItemTable()
        val arrayEntry = itemTable.root.children[0].findChildWithKey("bar")!!
        val firstObjEntry = arrayEntry.children.first()
        assertThat(firstObjEntry.children[1].value.createControl()!!.control.isDisabled, `is`(true))
        assertThat(firstObjEntry.children[2].value.createControl()!!.control.isDisabled, `is`(false))
    }

    @Test
    fun optionalArrayHasButtonToAddAndReset() {
        val schema = JSONObject("""{"type":"object","properties":{"bar":{"type":"array",
            "items":{"type":"string"}
            }}}""")
        editor.display("1", "1", JSONObject(), schema) { it }
        val itemTable = editor.getItemTable()
        val arrayEntry = itemTable.root.children[0].findChildWithKey("bar")!!
        assertThat(arrayEntry.value.createActions()!!.children.size, `is`(2))
        assertThat((arrayEntry.value.createActions()!!.children[0] as Button).text, `is`("↻"))
        assertThat((arrayEntry.value.createActions()!!.children[1] as Button).text, `is`("\uD83D\uDFA3"))
    }

    @Test
    fun arrayElementCanBeAddedByButtonPress() {
        val schema = JSONObject("""{"type":"object","properties":{"bar":{"type":"array",
            "items":{"type":"string"}
            }}}""")
        val data = JSONObject()
        editor.display("1", "1", data, schema) { it }
        val itemTable = editor.getItemTable()
        val arrayEntry = itemTable.root.children[0].findChildWithKey("bar")!!
        val addButton = arrayEntry.value.createActions()!!.children[1] as Button
        addButton.fire()
        addButton.fire()
        addButton.fire()
        assertThat(data.getJSONArray("bar").toList(), contains(null, null, null))
    }

    @Test
    fun addedArrayElementHasDefaultValue() {
        val schema = JSONObject("""{"type":"object","properties":{"bar":{"type":"array",
            "items":{"type":"string","default":"foo"}
            }}}""")
        val data = JSONObject()
        editor.display("1", "1", data, schema) { it }
        val itemTable = editor.getItemTable()
        val arrayEntry = itemTable.root.children[0].findChildWithKey("bar")!!
        val addButton = arrayEntry.value.createActions()!!.children[1] as Button
        addButton.fire()
        addButton.fire()
        assertThat(data.getJSONArray("bar").toList(), contains("foo", "foo"))
    }
}