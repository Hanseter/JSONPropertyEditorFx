package com.github.hanseter.json.editor

import javafx.scene.control.Spinner
import javafx.stage.Stage
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.`is`
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start

@ExtendWith(ApplicationExtension::class)
class NumberTest {
    lateinit var editor: JsonPropertiesEditor


    @Start
    fun start(stage: Stage) {
        editor = JsonPropertiesEditor()
    }

    @Test
    fun displayExistingValue() {
        val schema = JSONObject("""{"type":"object","properties":{"num":{"type":"number"}}}""")
        editor.display("1", "1", JSONObject().put("num", 723.168), schema) { JsonEditorData(it) }
        val itemTable = editor.getItemTable()
        val numberControl = editor.getControlInTable("num") as Spinner<Number>
        val converted = numberControl.valueFactory.converter.toString(723.168)
        MatcherAssert.assertThat(numberControl.editor.text, `is`(converted))
    }

    @Test
    fun modifyValueByTextInput() {
        val schema = JSONObject("""{"type":"object","properties":{"num":{"type":"number"}}}""")
        val data = JSONObject().put("num", 723.168)
        editor.display("1", "1", data, schema) { JsonEditorData(it) }
        val itemTable = editor.getItemTable()
        val numberControl = editor.getControlInTable("num") as Spinner<Number>
        val converted = numberControl.valueFactory.converter.toString(500.36)
        numberControl.editor.text = converted
        MatcherAssert.assertThat(data.getDouble("num"), `is`(500.36))
    }

    @ParameterizedTest
    @CsvSource(
            "false, 723.168,  1, 724.168",
            "false, 723.168, -1, 722.168",
            "true,       42,  1, 43",
            "true,       42, -1, 41"
    )
    fun modifyValueBySpinner(requireInt: Boolean, initialValue: Double, step: Int, result: Double) {
        val schema = JSONObject("""{"type":"object","properties":{"num":{"type":"number","requireInt":"$requireInt"}}}""")
        val data = JSONObject().put("num", initialValue)
        editor.display("1", "1", data, schema) { JsonEditorData(it) }
        val itemTable = editor.getItemTable()
        val numberControl = editor.getControlInTable("num") as Spinner<Number>
        numberControl.increment(step)
        val converted = numberControl.valueFactory.converter.toString(result)
        MatcherAssert.assertThat(numberControl.editor.text, `is`(converted))
    }

    @ParameterizedTest
    @CsvSource(
            "false, 42.5,  1, 43",
            "false, 42.5, -2, 41",
            "true,    42,  5, 43",
            "true,    42, -5, 41"
    )
    fun modifyValueBySpinnerInRange(requireInt: Boolean, initialValue: Double, step: Int, result: Double) {
        val schema = JSONObject("""{"type":"object","properties":{"num":{"type":"number",
            "requireInt":"$requireInt",
            "minimum":41,
            "maximum":43
}}}""")
        val data = JSONObject().put("num", initialValue)
        editor.display("1", "1", data, schema) { JsonEditorData(it) }
        val itemTable = editor.getItemTable()
        val numberControl = editor.getControlInTable("num") as Spinner<Number>
        numberControl.increment(step)
        val converted = numberControl.valueFactory.converter.toString(result)
        MatcherAssert.assertThat(numberControl.editor.text, `is`(converted))
    }

    @Test
    fun displaysCorrectDefaultValue() {
        val schema = JSONObject("""{"type":"object","properties":{"num":{"type":"number","default":723.168}}}""")
        editor.display("1", "1", JSONObject(), schema) { JsonEditorData(it) }
        val itemTable = editor.getItemTable()
        val numberControl = editor.getControlInTable("num") as Spinner<Number>
        val converted = numberControl.valueFactory.converter.toString(723.168)
        MatcherAssert.assertThat(numberControl.editor.text, `is`(converted))
    }

}