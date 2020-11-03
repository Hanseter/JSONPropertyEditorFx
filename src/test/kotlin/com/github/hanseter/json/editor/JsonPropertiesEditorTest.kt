package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.extensions.TreeItemData
import javafx.scene.control.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension


@ExtendWith(ApplicationExtension::class)
class JsonPropertiesEditorTest {

    @Test
    fun expandsOnlyTheFirstElements() {
        val editor = JsonPropertiesEditor(numberOfInitiallyOpenedObjects = 1)
        val schema = JSONObject("""{"type":"object","properties":{"foo":{"type":"string"}}}""")
        editor.display("1", "1", JSONObject(), schema) { it }
        editor.display("2", "2", JSONObject(), schema) { it }
        editor.display("3", "3", JSONObject(), schema) { it }
        val itemTable = editor.getItemTable()
        assertThat(itemTable.root.children.size, `is`(3))
        assertThat(itemTable.root.children[0].isExpanded, `is`(true))
        assertThat(itemTable.root.children[1].isExpanded, `is`(false))
        assertThat(itemTable.root.children[2].isExpanded, `is`(false))
    }

    @Test
    fun displaysSimpleObject() {
        val editor = JsonPropertiesEditor()
        val schema = JSONObject("""{"type":"object","properties":{"str":{"type":"string"}, "num":{"type":"number"}}}""")
        val data = JSONObject("""{"num":42.5,"str":"Hello"}""")
        editor.display("1", "1", data, schema) { it }
        val itemTable = editor.getItemTable()
        val objTable = itemTable.root.children.first()
        assertThat((objTable.findChildWithKey("str")!!.value.control as TextField).text, `is`("Hello"))
        val numberControl = (objTable.findChildWithKey("num")!!.value.control as Spinner<Number>)
        val converted = numberControl.valueFactory.converter.toString(42.5)
        assertThat(numberControl.editor.text, `is`(converted))
    }

    @Test
    fun modifySimpleObject() {
        val editor = JsonPropertiesEditor()
        val schema = JSONObject("""{"type":"object","properties":{"str":{"type":"string"}, "num":{"type":"number"}}}""")
        val data = JSONObject("""{"num":42.5,"str":"Hello"}""")
        var updateCount = 0
        editor.display("1", "1", data, schema) {
            updateCount++
            it
        }
        val itemTable = editor.getItemTable()
        val objTable = itemTable.root.children.first()
        val stringControl = (objTable.findChildWithKey("str")!!.value.control as TextField)
        stringControl.text = "foobar"
        assertThat(data.similar(JSONObject("""{"num":42.5,"str":"foobar"}""")), `is`(true))
        assertThat(updateCount, `is`(1))
        val numberControl = (objTable.findChildWithKey("num")!!.value.control as Spinner<Number>)
        numberControl.editor.text = "1573"
        assertThat(data.similar(JSONObject().put("str", "foobar").put("num", 1573.0)), `is`(true))
        assertThat(updateCount, `is`(2))
    }
}

fun JsonPropertiesEditor.getItemTable(): TreeTableView<TreeItemData> =
        (lookup("#contentArea") as ScrollPane).content as TreeTableView<TreeItemData>

fun TreeItem<TreeItemData>.findChildWithKey(key: String) =
        children.firstOrNull { it.value.label.text == key }