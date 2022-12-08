package com.github.hanseter.json.editor.base

import javafx.scene.input.KeyCode
import org.testfx.api.FxRobot

object FxRobotExt {
    fun FxRobot.typeHolingAlt(vararg keyCodes: KeyCode) {
        press(KeyCode.ALT)
        type(*keyCodes)
        release(KeyCode.ALT)
    }
}