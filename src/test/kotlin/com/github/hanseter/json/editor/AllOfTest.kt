package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.controls.TypeWithChildrenStatusControl
import com.github.hanseter.json.editor.util.PropertyGrouping
import com.github.hanseter.json.editor.util.ViewOptions
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
import org.testfx.util.WaitForAsyncUtils

@ExtendWith(ApplicationExtension::class)
class AllOfTest {

    lateinit var editor: JsonPropertiesEditor

    @Start
    fun start(stage: Stage) {
        editor = JsonPropertiesEditor(viewOptions = ViewOptions(groupBy = PropertyGrouping.NONE))
        editor.resolutionScopeProvider = ResolutionScopeProvider{ this::class.java.classLoader.getResource("")?.toURI() }
    }

    @Test
    fun nestedCompositeSchema() {
        val schema = loadSchema("nestedCompositeSchema.json")
        val data = JSONObject()
        editor.display("foo", "foo", data, schema) { it }
        val objectEntry = editor.getItemTable().root.children.first()
        assertThat(objectEntry.children.size, `is`(6))
        assertThat(editor.getControlInTable("fromNested"), `is`(instanceOf(TextField::class.java)))
        assertThat(editor.getControlInTable("additional"), `is`(instanceOf(Spinner::class.java)))
        assertThat(editor.getControlInTable("name"), `is`(instanceOf(TextField::class.java)))
        assertThat(editor.getControlInTable("x"), `is`(instanceOf(Spinner::class.java)))
        assertThat(editor.getControlInTable("y"), `is`(instanceOf(Spinner::class.java)))
        (editor.getControlInTable("name") as TextField).text = "Foobar"
        assertThat(data.keySet(), contains("name"))
        assertThat(data.getString("name"), `is`("Foobar"))
    }

    @Test
    fun compositeSchemaNotInRoot() {
        val schema = JSONObject("""{"type":"object","properties":{"notRoot":{"allOf": [
            {
      "${'$'}ref": "ReferencedPointSchema.json",
      "title": "notRoot"
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
        assertThat(objectEntry.children.size, `is`(0))
        (objectEntry.value.createControl()?.control as TypeWithChildrenStatusControl).button.fire()
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(objectEntry.children.size, `is`(3))
        assertThat(editor.getControlInTable("hello"), `is`(instanceOf(TextField::class.java)))
        assertThat(editor.getControlInTable("x"), `is`(instanceOf(Spinner::class.java)))
        assertThat(editor.getControlInTable("y"), `is`(instanceOf(Spinner::class.java)))
        (editor.getControlInTable("hello") as TextField).text = "Foobar"
        val numberControl = editor.getControlInTable("x") as Spinner<Number>
        numberControl.editor.text = numberControl.valueFactory.converter.toString(500.0)
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(data.keySet(), contains("notRoot"))
        val nestedData = data.getJSONObject("notRoot")
        assertThat(nestedData.keySet(), containsInAnyOrder("hello", "x"))
        assertThat(nestedData.getString("hello"), `is`("Foobar"))
        assertThat(nestedData.getDouble("x"), `is`(500.0))
    }

    @Test
    fun allOfWithReuqiredSubType() {
        val schema = loadSchema("completeValidationTestSchema.json")
        val data = JSONObject()
        editor.display("foo", "foo", data, schema) { it }
        val objectEntry = editor.getItemTable().root.children.first()
        val control = editor.getControlInTable(
                objectEntry.findChildWithKey("allOfs")!!
                        .findChildWithKey("required")!!
                        .findChildWithKey("a")!!.value) as TextField
        control.text = "Hello"
        assertThat(data.getJSONObject("allOfs").getJSONObject("required").getString("a"), `is`("Hello"))
    }

    private fun loadSchema(schemaName: String) =
            JSONObject(JSONTokener(this::class.java.classLoader.getResourceAsStream(schemaName)))
}