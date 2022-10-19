package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.types.TypeModel
import com.github.hanseter.json.editor.util.CustomizationObject
import javafx.scene.control.Label
import javafx.scene.control.Labeled
import javafx.scene.control.TextField
import javafx.stage.Stage
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start

@ExtendWith(ApplicationExtension::class)
class CustomizationObjectTest {

    lateinit var editor: JsonPropertiesEditor

    @Start
    fun start(stage: Stage) {
        editor = JsonPropertiesEditor()
    }

    @Test
    fun testTitleCustomization() {
        val schema = JSONObject("""
{
    "type":"object",
    "properties": {
        "foo": {
            "type": "string"
        },
        "notFoo": {
            "type": "string"
        }
    }
}""")
        val editor = JsonPropertiesEditor(
            customizationObject = object : CustomizationObject {

                override fun getTitle(model: TypeModel<*, *>, defaultTitle: String): String {
                    if (model.schema.pointer == listOf("foo")) {
                        return "bar" + model.value
                    }
                    return defaultTitle
                }

            }
        )

        val json = JSONObject("""{"foo":""}""")
        editor.display("1", "1", json, schema) { it }

        val fooCell = editor.getKeyCellInTable("bar")
        val notFooCell = editor.getKeyCellInTable("notFoo")

        val fooControl = editor.getControlInTable("bar") as TextField

        MatcherAssert.assertThat((fooCell.graphic as Labeled).text, Matchers.`is`("bar"))
        MatcherAssert.assertThat((notFooCell.graphic as Labeled).text, Matchers.`is`("notFoo"))

        fooControl.text = "something"

        MatcherAssert.assertThat((fooCell.graphic as Labeled).text, Matchers.`is`("barsomething"))
        MatcherAssert.assertThat((notFooCell.graphic as Labeled).text, Matchers.`is`("notFoo"))
    }

}