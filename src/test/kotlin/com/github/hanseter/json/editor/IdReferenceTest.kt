package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.ui.LabelledTextField
import com.github.hanseter.json.editor.util.IdRefDisplayMode
import impl.org.controlsfx.skin.AutoCompletePopup
import javafx.event.Event
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.stage.Stage
import org.everit.json.schema.StringSchema
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.api.FxRobot
import org.testfx.api.FxService
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start
import org.testfx.util.WaitForAsyncUtils
import java.util.stream.Stream

@ExtendWith(ApplicationExtension::class)
class IdReferenceTest {
    private val referenceProposalProvider = IdReferenceProposalProviderForTests()
    lateinit var editor: JsonPropertiesEditor
    lateinit var stage: Stage


    @Start
    fun start(stage: Stage) {
        editor = JsonPropertiesEditor()
        editor.referenceProposalProvider = referenceProposalProvider
        this.stage = stage
    }

    @Test
    fun descriptionAutoCompleted_triggersCallbackOnce(robot: FxRobot) {
        editor.viewOptions =
            editor.viewOptions.copy(idRefDisplayMode = IdRefDisplayMode.DESCRIPTION_ONLY)
        val refField = acceptFirstProposal(robot)
        assertThat(refField.text, `is`("description"))
        assertThat(refField.label, `is`(""))
    }

    @Test
    fun idWithDescriptionAutoCompleted_triggersCallbackOnce(robot: FxRobot) {
        editor.viewOptions =
            editor.viewOptions.copy(idRefDisplayMode = IdRefDisplayMode.ID_WITH_DESCRIPTION)
        val refField = acceptFirstProposal(robot)
        assertThat(refField.text, `is`("test2"))
        assertThat(refField.label, `is`("description"))
    }

    private fun acceptFirstProposal(robot: FxRobot): LabelledTextField {
        val schema =
            JSONObject("""{"type":"object","properties":{"ref":{"type":"string","format":"id-reference"}}}""")
        referenceProposalProvider.elements.add(
            ReferencableElement("test2", "description", JSONObject(), schema)
        )

        var triggerCount = 0
        editor.display("1", "1", JSONObject(), schema) {
            triggerCount++
            it
        }
        val refField = editor.getControlInTable("ref") as LabelledTextField

        WaitForAsyncUtils.asyncFx {
            stage.scene = Scene(refField)
            stage.show()
            refField.requestFocus()
        }
        WaitForAsyncUtils.waitForFxEvents()
        robot.type(KeyCode.T)
        val popup: AutoCompletePopup<*>
        var i = 0
        while (true) {
            val tmp = FxService.serviceContext().windowFinder.listWindows()
                .firstOrNull { it is AutoCompletePopup<*> }
            if (tmp != null) {
                popup = tmp as AutoCompletePopup<*>
                break
            }
            Thread.sleep(100)
            if (i++ > 10) throw AssertionError("AutoCompletePopup should have been shown")
        }
        assertThat(editor.valid.get(), `is`(false))
        triggerCount = 0
        WaitForAsyncUtils.asyncFx {
            Event.fireEvent(popup, AutoCompletePopup.SuggestionEvent(popup.suggestions.first()))
        }
        WaitForAsyncUtils.waitForFxEvents()

        assertThat(editor.valid.get(), `is`(true))
        assertThat(triggerCount, `is`(1))
        return refField
    }

    private class IdReferenceProposalProviderForTests : IdReferenceProposalProvider {
        val elements = mutableListOf<ReferencableElement>()
        val openedElements = mutableListOf<String>()

        override fun calcCompletionProposals(
            part: String?,
            editedElement: String,
            editedSchema: StringSchema,
            idRefMode: IdRefDisplayMode
        ): Stream<String> {
            if (part == null) return Stream.empty()
            return elements.stream().filter { it.id.startsWith(part) }.map { it.id }
        }

        override fun getReferenceDescription(
            reference: String?,
            editedElement: String,
            editedSchema: StringSchema
        ): String =
            elements.find { it.id == reference }?.description ?: ""

        override fun isValidReference(
            userInput: String?,
            editedElement: String,
            editedSchema: StringSchema
        ): Boolean =
            elements.find { it.id == userInput } != null

        override fun getDataAndSchema(id: String): IdReferenceProposalProvider.DataWithSchema? =
            elements.find { it.id == id }
                ?.let { IdReferenceProposalProvider.DataWithSchema(it.data, it.schema) }

    }


    private class ReferencableElement(
        val id: String,
        val description: String,
        val data: JSONObject,
        val schema: JSONObject,
    )
}