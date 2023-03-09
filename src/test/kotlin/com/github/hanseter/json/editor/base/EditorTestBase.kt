package com.github.hanseter.json.editor.base

import com.github.hanseter.json.editor.JsonPropertiesEditor
import com.github.hanseter.json.editor.ui.ControlTreeItemData
import javafx.scene.Scene
import javafx.scene.control.TreeTablePosition
import javafx.scene.control.TreeTableView
import javafx.stage.Stage
import org.json.JSONObject
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.api.FxRobot
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start
import org.testfx.util.WaitForAsyncUtils

@ExtendWith(ApplicationExtension::class)
open class EditorTestBase {

    lateinit var editor: JsonPropertiesEditor

    @Start
    open fun start(stage: Stage) {
        editor = JsonPropertiesEditor()
        stage.scene = Scene(editor, 800.0, 800.0)
        stage.show()
    }

    fun displayData(data: JSONObject, schema: JSONObject) {
        WaitForAsyncUtils.waitForAsyncFx(10000){
            editor.display("element-id", "element-title", data, schema) { it }
        }
        WaitForAsyncUtils.waitForFxEvents()
    }

    fun displayData(data: JSONObject, schemaName: String) {
        val schema=TestUtils.loadSchema(schemaName)
        displayData(data,schema)
    }

    fun getValueCells(robot: FxRobot): MutableSet<JsonPropertiesEditor.ValueCell> {
        return robot.lookup(".cell.control-cell").queryAll()
    }
    fun getCellValue(selectedCell: TreeTablePosition<out Any?, *>?) =
        (selectedCell?.treeItem?.value as ControlTreeItemData).typeControl.model.value

    fun getTable(robot: FxRobot)=robot.lookup(".tree-table-view").queryAs(TreeTableView::class.java)
}