package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.actions.ResetToDefaultAction
import com.github.hanseter.json.editor.actions.ResetToNullAction
import javafx.application.Application
import javafx.application.Platform
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

        val propEdit = JsonPropertiesEditor(ReferenceProvider, false, 2,
                customResolutionScopeProvider, listOf(ResetToDefaultAction, ResetToNullAction))
//        val testData = JSONObject().put("string", "bla47").put("somethingNotInSchema", "Hello")
//                .put("string list", listOf("A", "B"))
//                .put("string_list_readonly", listOf("A", "B"))
//                .put("referenced_point_schema", listOf(
//                        JSONObject().put("x", 1.0).put("y", 2.0),
//                        JSONObject().put("x", 3.0).put("y", 4.0)
//                ))
//                .put("enum", "bar")
//                .put("ref", "Hello")

//        val testData = JSONObject("""{
// "fromNested": "test",
// "additional": 9,
// "name": "waht?",
// "x": 6,
// "y": 5,
// "bool": true
//}""")
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

        val schema = JSONObject(JSONTokener(this::class.java.classLoader.getResourceAsStream(
//                "nestedCompositeSchema.json"
//                "resettableSchema.json"
                "deepSchema.json"
        )))
//		val schema = JSONObject(JSONTokener(this::class.java.getClassLoader().getResourceAsStream("StringSchema.json")))

//		propEdit.display("test4", "test4", testData, schema) { it }
//		propEdit.display("test5", "test5", testData, schema) { it }
//		propEdit.display("test6", "test6", testData, schema) { it }
//		propEdit.clear()
//		propEdit.display("test4", "test4", testData, schema) { it }
//		propEdit.display("test5", "test5", testData, schema) { it }
//		propEdit.display("test6", "test6", testData, schema) { it }

        propEdit.display("test", "isRoot 1 2 3 4 5 long text", resettableTestData, schema) {
            println(it.toString(1))
            Platform.runLater {
                propEdit.updateObject("test", it)
            }
            it
        }
//		propEdit.display("test2", "test2", testData, schema) { it }
//		propEdit.display("test3", "test3", testData, schema) { it }

        propEdit.valid.addListener { _, _, new -> println("Is valid: $new") }
        primaryStage.scene = Scene(propEdit, 800.0, 800.0)
        primaryStage.show()
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