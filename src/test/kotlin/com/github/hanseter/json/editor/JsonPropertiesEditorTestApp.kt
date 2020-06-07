package com.github.hanseter.json.editor

import javafx.application.Application
import javafx.stage.Stage
import javafx.scene.Scene
import org.json.JSONObject
import org.json.JSONTokener
import javafx.application.Platform

fun main(args: Array<String>) {
	Application.launch(JsonPropertiesEditorTestApp::class.java, *args)
}

class JsonPropertiesEditorTestApp : Application() {
	override fun start(primaryStage: Stage) {
		val propEdit = JsonPropertiesEditor(ReferenceProvider)
		val testData = JSONObject().put("string", "bla47").put("somethingNotInSchema", "Hello").put("number", 20)
			.put("string list", listOf("A", "B"))
			.put("enum", "bar")
			.put("ref", "Hello")
		val schema = JSONObject(JSONTokener(this::class.java.getClassLoader().getResourceAsStream("StringSchema.json")))
		propEdit.display("test", "title", testData, schema) {
			println(it.toString(1))
			Platform.runLater {
				propEdit.updateObject("test", it)
			}
			it
		}
		propEdit.display("test2", "test2", testData, schema) { it }
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

		override fun getReferenceDesciption(reference: String): String =
			possibleProposals.get(reference)?.first?.optString("name") ?: ""

		override fun isValidReference(userInput: String?): Boolean = possibleProposals.contains(userInput)

		override fun getDataAndSchema(id: String): Pair<JSONObject, JSONObject>? = possibleProposals.get(id)
	}
}