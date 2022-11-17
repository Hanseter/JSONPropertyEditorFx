/*
 * SWEETS - Software Engineering Tooling Suite
 *
 * Copyright (c) Siemens Mobility GmbH 2022, All Rights Reserved, Confidential.
 */
package com.github.hanseter.json.editor.base

import com.github.hanseter.json.editor.JsonPropertiesEditor
import com.github.hanseter.json.editor.ui.ControlTreeItemData
import com.github.hanseter.json.editor.ui.CustomTreeTableView
import com.sun.javafx.robot.FXRobot
import javafx.scene.Scene
import javafx.scene.control.TreeTablePosition
import javafx.scene.input.KeyCode
import javafx.stage.Stage
import org.json.JSONObject
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.api.FxRobot
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start
import org.testfx.util.WaitForAsyncUtils

/**
 *
 * @author Henrik Frühling (henrik.fruehling@siemens.com)
 */
@ExtendWith(ApplicationExtension::class)
open class EditorTestBase {

    lateinit var editor: JsonPropertiesEditor
    lateinit var robot: FxRobot

    @Start
    open fun start(stage: Stage) {
        editor = JsonPropertiesEditor()
        robot = FxRobot()
        stage.scene = Scene(editor, 1500.0, 1000.0)
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

    fun getValueCells(): MutableSet<JsonPropertiesEditor.ValueCell> {
        return robot.lookup(".cell.control-cell").queryAll()
    }
    fun getCellValue(selectedCell: TreeTablePosition<out Any?, *>?) =
        (selectedCell?.treeItem?.value as ControlTreeItemData).typeControl.model.value

    fun getTable()=robot.lookup(".tree-table-view").queryAs(CustomTreeTableView::class.java)
}