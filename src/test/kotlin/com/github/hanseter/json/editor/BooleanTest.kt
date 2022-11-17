package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.ui.skins.getNullableBoolean
import javafx.scene.control.CheckBox
import javafx.stage.Stage
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.json.JSONObject
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start
import java.util.stream.Stream

@ExtendWith(ApplicationExtension::class)
class BooleanTest {
    lateinit var editor: JsonPropertiesEditor

    @Start
    fun start(stage: Stage) {
        editor = JsonPropertiesEditor()
    }

    class NullableBooleanProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<Arguments> = Stream.of(
            *listOf(true, false, null)
                .mapIndexed { idx, it -> Arguments.of(it, idx) }
                .toTypedArray()
        )
    }

    @ParameterizedTest
    @ArgumentsSource(NullableBooleanProvider::class)
    fun displayExistingValue(value: Boolean?) {
        val schema = JSONObject("""{"type":"object","properties":{"bool":{"type":"boolean"}}}""")
        editor.display("1", "1", JSONObject().put("bool", value), schema) { it }
        val boolControl = editor.getControlInTable("bool") as CheckBox
        assertThat(boolControl.getNullableBoolean(), `is`(value))
    }

    @ParameterizedTest
    @ArgumentsSource(NullableBooleanProvider::class)
    fun displayDefaultValue(value: Boolean?) {
        val schema =
            JSONObject("""{"type":"object","properties":{"bool":{"type":"boolean","default":$value}}}""")
        editor.display("1", "1", JSONObject(), schema) { it }
        val boolControl = editor.getControlInTable("bool") as CheckBox
        assertThat(boolControl.getNullableBoolean(), `is`(value))
    }

    @ParameterizedTest
    @ArgumentsSource(NullableBooleanProvider::class)
    fun cycleBoolean(value: Boolean?, idx: Int) {
        val schema = JSONObject("""{"type":"object","properties":{"bool":{"type":"boolean"}}}""")
        val data = JSONObject().put("bool", value)
        editor.display("1", "1", data, schema) { it }
        val boolControl = editor.getControlInTable("bool") as CheckBox
        boolControl.fire()
        val currentValue = boolControl.getNullableBoolean()
        val nextValue = value?.not() ?: true
        assertThat(currentValue, `is`(nextValue))
    }
}