package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.ui.LabelledTextField
import com.github.hanseter.json.editor.util.IdRefDisplayMode
import javafx.stage.Stage
import org.everit.json.schema.StringSchema
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start
import org.testfx.util.WaitForAsyncUtils
import java.util.stream.Stream

@ExtendWith(ApplicationExtension::class)
class IdReferenceTest {
    private val referenceProposalProvider = IdReferenceProposalProviderForTests()
    lateinit var editor: JsonPropertiesEditor


    @Start
    fun start(stage: Stage) {
        editor = JsonPropertiesEditor()
        editor.referenceProposalProvider = referenceProposalProvider
    }

    @Test
    fun validatesReference() {
        val schema = JSONObject("""{"type":"object","properties":{"ref":{"type":"string","format":"id-reference"}}}""")
        referenceProposalProvider.elements.add(ReferencableElement("test2", "description", JSONObject(), schema, false))
        editor.display("1", "1", JSONObject(), schema) { it }
        val refField = editor.getControlInTable("ref") as LabelledTextField
        refField.text = "test"
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(false))
        refField.text = "test2"
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(true))
        assertThat(refField.label, `is`("description"))
    }

    private class IdReferenceProposalProviderForTests : IdReferenceProposalProvider {
        val elements = mutableListOf<ReferencableElement>()
        val openedElements = mutableListOf<String>()

        override fun calcCompletionProposals(part: String?, editedElement: String, editedSchema: StringSchema, idRefMode: IdRefDisplayMode): Stream<String> {
            if (part == null) return Stream.empty()
            return elements.stream().filter { it.id.startsWith(part) }.map { it.id }
        }

        override fun getReferenceDescription(reference: String?, editedElement: String, editedSchema: StringSchema): String =
                elements.find { it.id == reference }?.description ?: ""

        override fun isValidReference(userInput: String?, editedElement: String, editedSchema: StringSchema): Boolean =
                elements.find { it.id == userInput } != null

        override fun getDataAndSchema(id: String): IdReferenceProposalProvider.DataWithSchema? =
                elements.find { it.id == id }?.let { IdReferenceProposalProvider.DataWithSchema(it.data, it.schema) }

    }


    private class ReferencableElement(val id: String, val description: String, val data: JSONObject, val schema: JSONObject, val isOpenable: Boolean)
}