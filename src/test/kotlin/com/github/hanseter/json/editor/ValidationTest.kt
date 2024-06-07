package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.actions.TargetSelector
import com.github.hanseter.json.editor.types.SupportedType
import com.github.hanseter.json.editor.types.TypeModel
import com.github.hanseter.json.editor.validators.Validator
import javafx.scene.control.TextField
import org.controlsfx.validation.Severity
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.util.WaitForAsyncUtils

@ExtendWith(ApplicationExtension::class)
class ValidationTest {

    @Test
    fun `additional validators are used`() {

        val editor = JsonPropertiesEditor(
            additionalValidators = listOf(
                object : Validator {
                    override val selector: TargetSelector
                        get() = TargetSelector.SchemaType(SupportedType.SimpleType.StringType)

                    override fun validate(model: TypeModel<*, *>, objId: String): List<Validator.ValidationResult> {
                        return if (model.value == "foo") {
                            emptyList()
                        } else listOf(Validator.SimpleValidationResult(Severity.ERROR, "error"))
                    }

                }
            )
        )

        val schema = JSONObject("""
{
  "properties": {
    "a": {
      "type": "string"
    }
  }
}
        """)

        editor.display("1", "1", JSONObject().put("a", "foo"), schema) { it }

        WaitForAsyncUtils.waitForFxEvents()
        MatcherAssert.assertThat(editor.valid.get(), Matchers.`is`(true))

        val stringControl = editor.getControlInTable("a") as TextField

        stringControl.text = "bar"

        WaitForAsyncUtils.waitForFxEvents()
        MatcherAssert.assertThat(editor.valid.get(), Matchers.`is`(false))
    }

    @Test
    fun `additional validators are used if registered later`() {
        val editor = JsonPropertiesEditor()

        val schema = JSONObject("""
{
  "properties": {
    "a": {
      "type": "string"
    }
  }
}
        """)

        editor.display("1", "1", JSONObject().put("a", "foo"), schema) { it }

        WaitForAsyncUtils.waitForFxEvents()
        MatcherAssert.assertThat(editor.valid.get(), Matchers.`is`(true))

        editor.additionalValidators += object : Validator {
            override val selector: TargetSelector
                get() = TargetSelector.SchemaType(SupportedType.SimpleType.StringType)

            override fun validate(model: TypeModel<*, *>, objId: String): List<Validator.ValidationResult> {
                return if (model.value == "foo") {
                    emptyList()
                } else listOf(Validator.SimpleValidationResult(Severity.ERROR, "error"))
            }

        }

        WaitForAsyncUtils.waitForFxEvents()
        MatcherAssert.assertThat(editor.valid.get(), Matchers.`is`(true))

        val stringControl = editor.getControlInTable("a") as TextField

        stringControl.text = "bar"

        WaitForAsyncUtils.waitForFxEvents()
        MatcherAssert.assertThat(editor.valid.get(), Matchers.`is`(false))
    }

    @Test
    fun `changing validators triggers validation`() {
        val editor = JsonPropertiesEditor()

        val schema = JSONObject("""
{
  "properties": {
    "a": {
      "type": "string"
    }
  }
}
        """)

        editor.display("1", "1", JSONObject().put("a", "foo"), schema) { it }

        WaitForAsyncUtils.waitForFxEvents()
        MatcherAssert.assertThat(editor.valid.get(), Matchers.`is`(true))

        val stringControl = editor.getControlInTable("a") as TextField

        stringControl.text = "bar"

        WaitForAsyncUtils.waitForFxEvents()
        MatcherAssert.assertThat(editor.valid.get(), Matchers.`is`(true))

        editor.additionalValidators += object : Validator {
            override val selector: TargetSelector
                get() = TargetSelector.SchemaType(SupportedType.SimpleType.StringType)

            override fun validate(model: TypeModel<*, *>, objId: String): List<Validator.ValidationResult> {
                return if (model.value == "foo") {
                    emptyList()
                } else listOf(Validator.SimpleValidationResult(Severity.ERROR, "error"))
            }
        }

        WaitForAsyncUtils.waitForFxEvents()
        MatcherAssert.assertThat(editor.valid.get(), Matchers.`is`(false))

        editor.additionalValidators = emptyList()

        WaitForAsyncUtils.waitForFxEvents()
        MatcherAssert.assertThat(editor.valid.get(), Matchers.`is`(true))
    }

}