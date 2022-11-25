package com.github.hanseter.json.editor.ui

import com.github.hanseter.json.editor.base.EditorTestBase
import com.github.hanseter.json.editor.base.FxRobotExt.typeHolingAlt
import javafx.scene.input.KeyCode
import javafx.stage.Stage
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.Start

class NavigationTest : EditorTestBase() {

    @Start
    override fun start(stage: Stage) {
        super.start(stage)
    }

    @BeforeEach
    fun setup() {
        val array = (0..9).map { "$it" }
        displayData(JSONObject().put("additional", array), "simpleListSchema.json")
    }

    @Test
    fun `navigate up and down and type text`() {
        val testText = "changed!"
        val valueCells = getValueCells()
        robot.clickOn(valueCells.first())
        repeat(5) {
            robot.typeHolingAlt(KeyCode.DOWN)
        }
        //nav down
        repeat(2) {
            robot.typeHolingAlt(KeyCode.DOWN)
            robot.write(testText)
            val selectedCell = getTable().selectionModel.selectedCells.firstOrNull()
            assertThat(selectedCell, notNullValue())
            val cellValue = getCellValue(selectedCell)
            assertThat(cellValue, `is`(testText))
        }

        //nav up
        repeat(2) {
            robot.typeHolingAlt(KeyCode.UP)
            robot.write(testText + testText)
            val selectedCell = getTable().selectionModel.selectedCells.firstOrNull()
            assertThat(selectedCell, notNullValue())
            val cellValue = getCellValue(selectedCell)
            assertThat(cellValue, `is`(testText + testText))
        }
    }

    @Test
    fun `collapse and expand`(){
        val valueCell = getValueCells().first()
        robot.clickOn(valueCell)
        //collapse
        robot.typeHolingAlt(KeyCode.SUBTRACT)
        assertThat( getTable().root.children.first().isExpanded, `is`(false))
        //expand
        robot.typeHolingAlt(KeyCode.ADD)
        assertThat( getTable().root.children.first().isExpanded, `is`(true))

    }
}