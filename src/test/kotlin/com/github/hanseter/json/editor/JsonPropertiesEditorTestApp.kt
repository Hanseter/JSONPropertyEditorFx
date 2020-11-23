package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.util.PropertyGrouping
import com.github.hanseter.json.editor.util.ViewOptions
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import org.json.JSONObject
import org.json.JSONTokener
import java.net.URI

fun main(args: Array<String>) {
    Application.launch(JsonPropertiesEditorTestApp::class.java, *args)
}

class JsonPropertiesEditorTestApp : Application() {
    override fun start(primaryStage: Stage) {
        val customResolutionScopeProvider = object : ResolutionScopeProvider {
            override fun getResolutionScopeForElement(elementId: String): URI? =
                    this::class.java.classLoader.getResource("")?.toURI()
        }

        val viewOptions = ViewOptions(
                markRequired = true,
                groupBy = PropertyGrouping.NONE
        )

        val propEdit = JsonPropertiesEditor(ReferenceProvider, false, 2,
                customResolutionScopeProvider, viewOptions)
        val resettableTestData = JSONObject("""
{
  "reqBool": true,
  "reqInt": 5,
  "reqDouble": 42.24
}
""")

        val nestedSchemaTestData = JSONObject("""
            {
             "additional": 312,
             "x": 12,
             "name": "MyName",
             "y": 42
            }
        """)

        val completeValidationInvalidData = JSONObject("""
{
  "strings": {
    "maxLength": "abcdefghi",
    "minLength": "a"
  },
  "lists": {
    "minItems": ["foo"],
    "uniqueItems": ["d", "d"]
  },
  "id-references": {
    "pattern": "H"
  }
}
        """)

//        "nestedCompositeSchema.json"
//                "resettableSchema.json"
//                "deepSchema.json"
//                "completeValidationTestSchema.json"
//        "StringSchema.json"

        display(propEdit, "completeValidationTestSchema.json", JSONObject())
//        displayElementWithOneOf(propEdit)

        propEdit.valid.addListener { _, _, new -> println("Is valid: $new") }
        primaryStage.scene = Scene(propEdit, 800.0, 800.0)
        primaryStage.show()
    }

    private fun display(editor: JsonPropertiesEditor, schemaName: String, data: JSONObject) {
        editor.display("test", "isRoot 1 2 3 4 5 long text", data, loadSchema(schemaName)) {
            println(it.toString(1))
            it
        }
    }

    private fun loadSchema(schemaName: String) =
            JSONObject(JSONTokener(this::class.java.classLoader.getResourceAsStream(schemaName)))

    private fun displayElementWithOneOf(editor: JsonPropertiesEditor) {
        val schema = JSONObject("""{
"type":"object",
"properties":{
"choice": {
"oneOf":[
    {"type":"string"},
    {"type":"number"}
]}}}""")
        editor.display("1", "1", JSONObject().put("choice", JSONObject.NULL), schema) {
            println(it.toString(1))
            it
        }
    }

    object ReferenceProvider : IdReferenceProposalProvider {
        private val possibleProposals =
                mapOf(
                        "Hello" to IdReferenceProposalProvider.DataWithSchema(
                                JSONObject().put("name", "world").put("ref", "Foo"),
                                JSONObject(JSONTokener(this::class.java.classLoader.getResourceAsStream("TestSchema2.json")))
                        ),
                        "Goodbye" to IdReferenceProposalProvider.DataWithSchema(
                                JSONObject().put("name", "my love"),
                                JSONObject(JSONTokener(this::class.java.classLoader.getResourceAsStream("TestSchema2.json")))
                        ),
                        "Foo" to IdReferenceProposalProvider.DataWithSchema(
                                JSONObject().put("name", "Bar"),
                                JSONObject(JSONTokener(this::class.java.classLoader.getResourceAsStream("TestSchema2.json")))
                        ),
                        "Complex" to IdReferenceProposalProvider.DataWithSchema(
                                JSONObject(mapOf(
                                        "name" to "Object",
                                        "nameWithMore" to "Also Object",
                                        "anInt" to 42,
                                        "aBool" to true,
                                        "sub" to mapOf(
                                                "sub1" to 42.5,
                                                "sub2" to "subString",
                                                "list" to listOf(
                                                        "a",
                                                        1,
                                                        mapOf(
                                                                "deeply" to "nested"
                                                        )
                                                )
                                        ),
                                        "propWith/Escaped~Values" to false
                                )),
                                JSONObject(JSONTokener(this::class.java.classLoader.getResourceAsStream("TestSchema2.json")))
                        )
                )

        override fun calcCompletionProposals(part: String?): List<String> =
                possibleProposals.keys.filter { it.startsWith(part ?: "") }

        override fun getReferenceDescription(reference: String?): String =
                possibleProposals[reference]?.data?.optString("name") ?: ""

        override fun isValidReference(userInput: String?): Boolean = possibleProposals.contains(userInput)

        override fun getDataAndSchema(id: String): IdReferenceProposalProvider.DataWithSchema? = possibleProposals[id]
        override fun isOpenable(id: String) = true
        override fun openElement(id: String) {
            println("Request to open $id")
        }
    }
}
