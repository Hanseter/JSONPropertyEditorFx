package com.github.hanseter.json.editor

import javafx.stage.Stage
import org.controlsfx.control.ToggleSwitch
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.json.JSONObject
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start

@ExtendWith(ApplicationExtension::class)
class BooleanTest {
    lateinit var editor: JsonPropertiesEditor

    @Start
    fun start(stage: Stage) {
        editor = JsonPropertiesEditor()
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun displayExistingValue(value: Boolean) {
        val schema = JSONObject("""{"type":"object","properties":{"bool":{"type":"boolean"}}}""")
        editor.display("1", "1", JSONObject().put("bool", value), schema) { it }
        val boolControl = editor.getControlInTable("bool") as ToggleSwitch
        assertThat(boolControl.isSelected, `is`(value))
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun displayDefaultValue(value: Boolean) {
        val schema = JSONObject("""{"type":"object","properties":{"bool":{"type":"boolean","default":$value}}}""")
        editor.display("1", "1", JSONObject(), schema) { it }
        val boolControl = editor.getControlInTable("bool") as ToggleSwitch
        assertThat(boolControl.isSelected, `is`(value))
    }
}