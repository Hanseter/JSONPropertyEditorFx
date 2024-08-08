package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.base.TestUtils.waitForAsyncFx
import com.github.hanseter.json.editor.util.ViewOptions
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
import org.testfx.util.WaitForAsyncUtils
import java.text.DecimalFormatSymbols
import kotlin.math.pow

@ExtendWith(ApplicationExtension::class)
class NumberTest {
    lateinit var editor: JsonPropertiesEditor


    @Start
    fun start(stage: Stage) {
        editor = JsonPropertiesEditor(
            viewOptions = ViewOptions(
                decimalFormatSymbols = DecimalFormatSymbols().apply {
                    decimalSeparator = '.'
                    groupingSeparator = ' '
                })
        )
    }

    @Test
    fun displayExistingValue() {
        val schema = JSONObject("""{"type":"object","properties":{"num":{"type":"number"}}}""")
        waitForAsyncFx {
            editor.display("1", "1", JSONObject().put("num", 723.168), schema) { it }
        }
        val itemTable = editor.getItemTable()
        val numberControl = editor.getControlInTable("num") as Spinner<Number>
        val converted = numberControl.valueFactory.converter.toString(723.168)
        MatcherAssert.assertThat(numberControl.editor.text, `is`(converted))
    }

    @Test
    fun modifyValueByTextInput() {
        val schema = JSONObject("""{"type":"object","properties":{"num":{"type":"number"}}}""")
        val data = JSONObject().put("num", 723.168)
        editor.display("1", "1", data, schema) { it }
        val numberControl = editor.getControlInTable("num") as Spinner<Number>
        val converted = numberControl.valueFactory.converter.toString(500.36)
        WaitForAsyncUtils.asyncFx {
            numberControl.editor.text = converted
        }
        WaitForAsyncUtils.waitForFxEvents()
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
        val schema =
            JSONObject("""{"type":"object","properties":{"num":{"type":"number","requireInt":"$requireInt"}}}""")
        val data = JSONObject().put("num", initialValue)
        editor.display("1", "1", data, schema) { it }
        val itemTable = editor.getItemTable()
        val numberControl = editor.getControlInTable("num") as Spinner<Number>
        WaitForAsyncUtils.waitForFxEvents()
        WaitForAsyncUtils.waitForAsync(10000) {
            numberControl.increment(step)
        }
        WaitForAsyncUtils.waitForFxEvents()
        val converted = numberControl.valueFactory.converter.toString(result)
        MatcherAssert.assertThat(numberControl.editor.text, `is`(converted))
    }

    @ParameterizedTest
    @CsvSource(
        "false, 42.5,  1, 43.5",
        "false, 42.5, -2, 40.5",
        "true,    42,  5, 47",
        "true,    42, -5, 37"
    )
    fun modifyValueBySpinnerInRange(
        requireInt: Boolean,
        initialValue: Double,
        step: Int,
        result: Double
    ) {
        val schema = JSONObject(
            """{"type":"object","properties":{"num":{"type":"number",
            "requireInt":"$requireInt",
            "minimum":41,
            "maximum":43
}}}"""
        )
        val data = JSONObject().put("num", initialValue)
        editor.display("1", "1", data, schema) { it }
        val itemTable = editor.getItemTable()
        val numberControl = editor.getControlInTable("num") as Spinner<Number>
        WaitForAsyncUtils.waitForFxEvents()
        MatcherAssert.assertThat(editor.valid.get(), `is`(true))
        WaitForAsyncUtils.waitForAsync(10000) {
            numberControl.increment(step)
        }
        WaitForAsyncUtils.waitForFxEvents()
        val converted = numberControl.valueFactory.converter.toString(result)
        MatcherAssert.assertThat(numberControl.editor.text, `is`(converted))
        MatcherAssert.assertThat(editor.valid.get(), `is`(false))
    }

    @Test
    fun displaysCorrectDefaultValue() {
        val schema =
            JSONObject("""{"type":"object","properties":{"num":{"type":"number","default":723.168}}}""")
        waitForAsyncFx {
            editor.display("1", "1", JSONObject(), schema) { it }
        }
        val itemTable = editor.getItemTable()
        val numberControl = editor.getControlInTable("num") as Spinner<Number>
        val converted = numberControl.valueFactory.converter.toString(723.168)
        MatcherAssert.assertThat(numberControl.editor.text, `is`(converted))
    }

    @ParameterizedTest
    @CsvSource(
        delimiter = ';', value = [
            "0; 0; 123; 124",
            "0.0; 0; 123; 124.0",
            "0.00; 0; 123; 124.00",
            "0.000; 0; 123; 124.000",
            "0.000; 1; 1234; 124.400",
            "0.000; 2; 12345; 124.450",
            "0.000; 3; 123456; 124.456",
            "0,000.000; 3; 1234567; 1 235.567",
        ]
    )
    fun displaysFormattedInteger(
        pattern: String,
        precision: Int,
        initialValue: Int,
        expectedText: String
    ) {
        val schema = JSONObject()
            .put("type", "object")
            .put(
                "properties", JSONObject()
                    .put(
                        "num", JSONObject()
                            .put("type", "integer")
                            .put(
                                "int-format", JSONObject()
                                    .put("pattern", pattern)
                                    .put("precision", precision)
                            )
                    )
            )
        val multiplier = 10.0.pow(precision).toInt()
        editor.display("1", "1", JSONObject().put("num", initialValue), schema) { it }
        val itemTable = editor.getItemTable()
        val numberControl = editor.getControlInTable("num") as Spinner<Int?>
        WaitForAsyncUtils.waitForFxEvents()
        WaitForAsyncUtils.waitForAsync(10000) {
            numberControl.increment()
        }
        WaitForAsyncUtils.waitForFxEvents()
        val converted =
            numberControl.valueFactory.converter.toString(initialValue + multiplier)
        MatcherAssert.assertThat(numberControl.editor.text, `is`(converted))
        MatcherAssert.assertThat(numberControl.editor.text, `is`(expectedText))
    }

}