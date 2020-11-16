package com.github.hanseter.json.editor

import javafx.scene.control.Spinner
import javafx.scene.control.TextField
import javafx.stage.Stage
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.json.JSONObject
import org.json.JSONTokener
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start

@ExtendWith(ApplicationExtension::class)
class AllOfTest {

    lateinit var editor: JsonPropertiesEditor

    @Start
    fun start(stage: Stage) {
        editor = JsonPropertiesEditor(resolutionScopeProvider = { this::class.java.classLoader.getResource("")?.toURI() })
    }

    @Test
    fun nestedCompositeSchema() {
        val schema = loadSchema("nestedCompositeSchema.json")
        val data = JSONObject()
        editor.display("foo", "foo", data, schema) { it }
        val objectEntry = editor.getItemTable().root.children.first()
        //5 fields + 1 header
        assertThat(objectEntry.children.size, `is`(6))
        assertThat(objectEntry.findChildWithKey("fromNested")?.value?.control, `is`(instanceOf(TextField::class.java)))
        assertThat(objectEntry.findChildWithKey("additional")?.value?.control, `is`(instanceOf(Spinner::class.java)))
        assertThat(objectEntry.findChildWithKey("name")?.value?.control, `is`(instanceOf(TextField::class.java)))
        assertThat(objectEntry.findChildWithKey("x")?.value?.control, `is`(instanceOf(Spinner::class.java)))
        assertThat(objectEntry.findChildWithKey("y")?.value?.control, `is`(instanceOf(Spinner::class.java)))
        (objectEntry.findChildWithKey("name")?.value?.control as TextField).text = "Foobar"
        assertThat(data.keySet(), contains("name"))
        assertThat(data.getString("name"), `is`("Foobar"))
    }

    @Test
    fun compositeSchemaNotInRoot() {
        val schema = JSONObject("""{"type":"object","properties":{"notRoot":{"allOf": [
            {
      "${'$'}ref": "ReferencedPointSchema.json"
    },{
      "type": "object",
      "properties": {
        "hello": {
          "type": "string"
        }
      }
    }]}}}""")

        val data = JSONObject()
        editor.display("foo", "foo", data, schema) { it }
        val objectEntry = editor.getItemTable().root.children.first().findChildWithKey("notRoot")!!
        //3 fields + 1 header
        assertThat(objectEntry.children.size, `is`(4))
        assertThat(objectEntry.findChildWithKey("hello")?.value?.control, `is`(instanceOf(TextField::class.java)))
        assertThat(objectEntry.findChildWithKey("x")?.value?.control, `is`(instanceOf(Spinner::class.java)))
        assertThat(objectEntry.findChildWithKey("y")?.value?.control, `is`(instanceOf(Spinner::class.java)))
        (objectEntry.findChildWithKey("hello")?.value?.control as TextField).text = "Foobar"
        val numberControl = objectEntry.findChildWithKey("x")?.value?.control as Spinner<Number>
        numberControl.editor.text = numberControl.valueFactory.converter.toString(500.0)
        assertThat(data.keySet(), contains("notRoot"))
        val nestedData = data.getJSONObject("notRoot")
        assertThat(nestedData.keySet(), containsInAnyOrder("hello", "x"))
        assertThat(nestedData.getString("hello"), `is`("Foobar"))
        assertThat(nestedData.getDouble("x"), `is`(500.0))
    }


    private fun loadSchema(schemaName: String) =
            JSONObject(JSONTokener(this::class.java.classLoader.getResourceAsStream(schemaName)))
}