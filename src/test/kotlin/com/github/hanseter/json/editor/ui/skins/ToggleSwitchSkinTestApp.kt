/*
 * SWEETS - Software Engineering Tooling Suite
 *
 * Copyright (c) Siemens Mobility GmbH 2022, All Rights Reserved, Confidential.
 */
package com.github.hanseter.json.editor.ui.skins

import javafx.application.Application
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.CheckBox
import javafx.scene.layout.GridPane
import javafx.stage.Stage

/**
 *
 * @author Henrik Frühling (henrik.fruehling@siemens.com)
 */
class ToggleSwitchSkinTestApp : Application() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(ToggleSwitchSkinTestApp::class.java, *args)
        }

        fun CheckBox.withToggleSkin() = this.apply {
            skin = ToggleSwitchSkin(this)
        }
    }

    override fun start(primaryStage: Stage) {
        val checkBoxNoLabelTrue = CheckBox().withToggleSkin().apply {
            isSelected = true
        }
        val checkBoxNoLabelFalse = CheckBox().withToggleSkin().apply {
            isSelected = false
        }
        val checkBoxNoLabelNull = CheckBox().withToggleSkin().apply {
            isIndeterminate = true
        }
        val checkBoxTrue = CheckBox("true").withToggleSkin().apply {
            isSelected = true
        }
        val checkBoxFalse = CheckBox("false").withToggleSkin().apply {
            isSelected = false
        }
        val checkBoxNull = CheckBox("Null").withToggleSkin().apply {
            isIndeterminate = true
        }
        val checkBoxDisabled = CheckBox("Disabled").withToggleSkin().apply {
            isIndeterminate = true
            isDisable = true
        }
        val root = GridPane().apply {
            vgap = 10.0
            hgap = 10.0
            alignment = Pos.CENTER
            addRow(0, checkBoxNoLabelTrue, checkBoxNoLabelFalse, checkBoxNoLabelNull)
            addRow(1, checkBoxTrue, checkBoxFalse, checkBoxNull)
            addRow(2, checkBoxDisabled)
            addRow(3, CheckBox(), CheckBox("default"))
        }
        primaryStage.scene = Scene(root, 400.0, 400.0)

        primaryStage.show()

    }
}