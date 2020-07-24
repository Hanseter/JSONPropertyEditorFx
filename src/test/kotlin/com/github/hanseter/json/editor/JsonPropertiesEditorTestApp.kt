package com.github.hanseter.json.editor

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
			override fun getResolutionScopeForElement(elementId: String): URI? {
				return this::class.java.classLoader.getResource("").toURI()
			}
		}

		val propEdit = JsonPropertiesEditor(ReferenceProvider, false, 2, customResolutionScopeProvider)
		val testData = JSONObject().put("string", "bla47").put("somethingNotInSchema", "Hello")
			.put("string list", listOf("A", "B"))
				.put("string_list_readonly", listOf("A", "B"))
				.put("referenced_point_schema", listOf(
						JSONObject().put("x", 1.0).put("y", 2.0),
						JSONObject().put("x", 3.0).put("y", 4.0)
				))
				.put("enum", "bar")
				.put("ref", "Hello")

		val schema = JSONObject(JSONTokener(this::class.java.getClassLoader().getResourceAsStream("StringSchema.json")))

		propEdit.display("test4", "test4", testData, schema) { it }
		propEdit.display("test5", "test5", testData, schema) { it }
		propEdit.display("test6", "test6", testData, schema) { it }
		propEdit.clear()
		propEdit.display("test4", "test4", testData, schema) { it }
		propEdit.display("test5", "test5", testData, schema) { it }
		propEdit.display("test6", "test6", testData, schema) { it }

		propEdit.display("test", "isRoot 1 2 3 4 5 long text", testData, schema) {
			println(it.toString(1))
			Platform.runLater {
				propEdit.updateObject("test", it)
			}
			it
		}
		propEdit.display("test2", "test2", testData, schema) { it }
		propEdit.display("test3", "test3", testData, schema) { it }

		propEdit.valid.addListener { _, _, new -> println("Is valid: $new") }
		primaryStage.setScene(Scene(propEdit, 800.0, 800.0))
		primaryStage.show()
	}

	object ReferenceProvider : IdReferenceProposalProvider {
		private val possibleProposals =
			mapOf(
				"Hello" to Pair<JSONObject, JSONObject>(
					JSONObject().put("name", "world").put("ref", "Foo"),
					JSONObject(JSONTokener(this::class.java.classLoader.getResourceAsStream("TestSchema2.json")))
				),
				"Goodbye" to Pair<JSONObject, JSONObject>(
					JSONObject().put("name", "my love"),
					JSONObject(JSONTokener(this::class.java.classLoader.getResourceAsStream("TestSchema2.json")))
				),
				"Foo" to Pair<JSONObject, JSONObject>(
					JSONObject().put("name", "Bar"),
					JSONObject(JSONTokener(this::class.java.classLoader.getResourceAsStream("TestSchema2.json")))
				)
			)

		override fun calcCompletionProposals(part: String): List<String> =
			possibleProposals.keys.filter { it.startsWith(part) }

		override fun getReferenceDescription(reference: String): String =
			possibleProposals.get(reference)?.first?.optString("name") ?: ""

		override fun isValidReference(userInput: String?): Boolean = possibleProposals.contains(userInput)

		override fun getDataAndSchema(id: String): Pair<JSONObject, JSONObject>? = possibleProposals.get(id)
		override fun isOpenable(id: String) = true
		override fun openElement(id: String) {
			println("Request to open " + id)
		}
	}
}