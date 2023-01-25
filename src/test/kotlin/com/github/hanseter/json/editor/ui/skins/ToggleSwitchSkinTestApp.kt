package com.github.hanseter.json.editor.ui.skins

import javafx.application.Application
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.CheckBox
import javafx.scene.layout.GridPane
import javafx.stage.Stage

fun main(args: Array<String>) {
    Application.launch(ToggleSwitchSkinTestApp::class.java, *args)
}

class ToggleSwitchSkinTestApp : Application() {
    companion object {

        fun CheckBox.withToggleSkin() = this.apply {
            skin = ToggleSwitchSkin(this)
        }
    }

    override fun start(primaryStage: Stage) {
        val root = GridPane().apply {
            vgap = 10.0
            hgap = 10.0
            alignment = Pos.CENTER

            createCheckBoxes().forEachIndexed { row, checkBoxes ->
                checkBoxes.forEach {
                    addRow(row, it.apply { it.withToggleSkin() })
                }
            }
            createCheckBoxes().forEachIndexed { row, checkBoxes ->
                checkBoxes.forEach {
                    addRow(row, it)
                }
            }
        }
        primaryStage.scene = Scene(root, 400.0, 400.0)

        primaryStage.show()

    }

    fun createCheckBoxes(): List<List<CheckBox>> {
        val checkBoxNoLabelTrue = CheckBox().apply {
            isSelected = true
        }
        val checkBoxNoLabelFalse = CheckBox().apply {
            isSelected = false
        }
        val checkBoxNoLabelNull = CheckBox().apply {
            isIndeterminate = true
        }
        val checkBoxTrue = CheckBox("true").apply {
            isSelected = true
        }
        val checkBoxFalse = CheckBox("false").apply {
            isSelected = false
        }
        val checkBoxNull = CheckBox("Null").apply {
            isIndeterminate = true
        }
        val checkBoxDisabledTrue = CheckBox("true").apply {
            isSelected = true
            isDisable = true
        }
        val checkBoxDisabledFalse = CheckBox("false").apply {
            isSelected = false
            isDisable = true
        }
        val checkBoxDisabledNull = CheckBox("Null").apply {
            isIndeterminate = true
            isDisable = true
        }
        return listOf(
            listOf(checkBoxNoLabelTrue, checkBoxNoLabelFalse, checkBoxNoLabelNull),
            listOf(checkBoxTrue, checkBoxFalse, checkBoxNull),
            listOf(checkBoxDisabledTrue, checkBoxDisabledFalse, checkBoxDisabledNull),
        )
    }
}