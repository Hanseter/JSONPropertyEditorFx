package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.ui.TreeItemData
import com.github.hanseter.json.editor.util.ViewOptions
import javafx.scene.Node
import javafx.scene.control.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.util.WaitForAsyncUtils


@ExtendWith(ApplicationExtension::class)
class JsonPropertiesEditorTest {

    @Test
    fun expandsOnlyTheFirstElements() {
        val editor = JsonPropertiesEditor(viewOptions = ViewOptions(numberOfInitiallyOpenedObjects = 1))
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

        assertThat((editor.getControlInTable("str") as TextField).text, `is`("Hello"))
        val numberControl = editor.getControlInTable("num") as Spinner<Number>
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
        val stringControl = (editor.getControlInTable("str") as TextField)
        stringControl.text = "foobar"
        assertThat(data.similar(JSONObject("""{"num":42.5,"str":"foobar"}""")), `is`(true))
        assertThat(updateCount, `is`(1))
        val numberControl = (editor.getControlInTable("num") as Spinner<Number>)
        numberControl.editor.text = "1573"
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(data.similar(JSONObject().put("str", "foobar").put("num", 1573.0)), `is`(true))
        assertThat(updateCount, `is`(2))
    }

    @Test
    fun modifySimpleObjectInCallback() {
        val editor = JsonPropertiesEditor()
        val schema = JSONObject("""{"type":"object","properties":{"str":{"type":"string"}, "num":{"type":"number"}}}""")
        val data = JSONObject("""{"num":42.5,"str":"Hello"}""")
        editor.display("1", "1", data, schema) {
            it.put("num", 17)
            it
        }
        val numberControl = (editor.getControlInTable("num") as Spinner<Number>)
        val stringControl = (editor.getControlInTable("str") as TextField)
        WaitForAsyncUtils.waitForAsyncFx(10000){
            stringControl.text = "foobar"
        }
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(numberControl.editor.text, `is`("17"))
    }
}

fun JsonPropertiesEditor.getItemTable(): TreeTableView<TreeItemData> =
        (lookup("#contentArea") as ScrollPane).content as TreeTableView<TreeItemData>

fun TreeItem<TreeItemData>.findChildWithKey(key: String): TreeItem<TreeItemData>? =
        children.firstOrNull { it.value.title == key }

fun TreeItem<TreeItemData>.findChildWithKeyRecursive(key: String): TreeItem<TreeItemData>? =
        findChildWithKey(key)
                ?: children.firstNotNullOfOrNull { it.findChildWithKeyRecursive(key) }

fun JsonPropertiesEditor.getControlInTable(key: String): Node =
        getControlInTable(getItemTable().root.findChildWithKeyRecursive(key)?.value)

fun JsonPropertiesEditor.getControlInTable(itemData: TreeItemData?): Node {
    val table = getItemTable()
    val column: TreeTableColumn<TreeItemData, TreeItemData> = table.columns[1] as TreeTableColumn<TreeItemData, TreeItemData>
    val cell: JsonPropertiesEditor.ValueCell = column.cellFactory.call(column) as JsonPropertiesEditor.ValueCell
    cell.updateItem(itemData, false)
    return cell.graphic
}

fun JsonPropertiesEditor.getKeyCellInTable(key: String): JsonPropertiesEditor.KeyCell =
    getKeyCellInTable(getItemTable().root.findChildWithKeyRecursive(key)?.value)

fun JsonPropertiesEditor.getKeyCellInTable(itemData: TreeItemData?): JsonPropertiesEditor.KeyCell {
    val table = getItemTable()
    val column = table.columns[0] as TreeTableColumn<TreeItemData, TreeItemData>
    val cell = column.cellFactory.call(column) as JsonPropertiesEditor.KeyCell
    cell.updateItem(itemData, false)
    return cell
}