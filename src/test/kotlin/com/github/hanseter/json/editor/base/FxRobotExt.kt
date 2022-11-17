/*
 * SWEETS - Software Engineering Tooling Suite
 *
 * Copyright (c) Siemens Mobility GmbH 2022, All Rights Reserved, Confidential.
 */
package com.github.hanseter.json.editor.base

import javafx.scene.input.KeyCode
import org.testfx.api.FxRobot

/**
 *
 * @author Henrik Frühling (henrik.fruehling@siemens.com)
 */
object FxRobotExt {
    fun FxRobot.typeHolingAlt(vararg keyCodes: KeyCode) {
        press(KeyCode.ALT)
        type(*keyCodes)
        release(KeyCode.ALT)
    }
}