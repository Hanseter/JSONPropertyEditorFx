package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.util.LabelledTextField
import javafx.stage.Stage
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start
import org.testfx.util.WaitForAsyncUtils

@ExtendWith(ApplicationExtension::class)
class IdReferenceTest {
    private val referenceProposalProvider = IdReferenceProposalProviderForTests()
    lateinit var editor: JsonPropertiesEditor


    @Start
    fun start(stage: Stage) {
        editor = JsonPropertiesEditor(referenceProposalProvider)
    }

    @Test
    fun validatesReference() {
        val schema = JSONObject("""{"type":"object","properties":{"ref":{"type":"string","format":"id-reference"}}}""")
        referenceProposalProvider.elements.add(ReferencableElement("test2", "description", JSONObject(), schema, false))
        editor.display("1", "1", JSONObject(), schema) { it }
        val itemTable = editor.getItemTable()
        val refField = itemTable.root.children.first().findChildWithKey("ref")!!.value.control as LabelledTextField
        refField.text = "test"
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(false))
        refField.text = "test2"
        WaitForAsyncUtils.waitForFxEvents()
        assertThat(editor.valid.get(), `is`(true))
        assertThat(refField.label, `is`(" (description)"))
    }

    private class IdReferenceProposalProviderForTests : IdReferenceProposalProvider {
        val elements = mutableListOf<ReferencableElement>()
        val openedElements = mutableListOf<String>()

        override fun calcCompletionProposals(part: String?): List<String> {
            if (part == null) return emptyList()
            return elements.filter { it.id.startsWith(part) }.map { it.id }
        }

        override fun getReferenceDescription(reference: String?): String =
                elements.find { it.id == reference }?.description ?: ""

        override fun isValidReference(userInput: String?): Boolean {
            return elements.find { it.id == userInput } != null
        }

        override fun isOpenable(id: String): Boolean =
                elements.find { it.id == id }?.isOpenable ?: false

        override fun getDataAndSchema(id: String): IdReferenceProposalProvider.DataWithSchema? =
                elements.find { it.id == id }?.let { IdReferenceProposalProvider.DataWithSchema(it.data, it.schema) }

        override fun openElement(id: String) {
            openedElements.add(id)
        }
    }


    private class ReferencableElement(val id: String, val description: String, val data: JSONObject, val schema: JSONObject, val isOpenable: Boolean)
}