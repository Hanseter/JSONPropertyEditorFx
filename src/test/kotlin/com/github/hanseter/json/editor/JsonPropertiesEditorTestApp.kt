package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.ui.PropertiesEditorToolbar
import com.github.hanseter.json.editor.util.IdRefDisplayMode
import com.github.hanseter.json.editor.util.PropertyGrouping
import com.github.hanseter.json.editor.util.ViewOptions
import javafx.application.Application
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.everit.json.schema.StringSchema
import org.json.JSONObject
import org.json.JSONTokener
import java.net.URI
import java.util.stream.Stream

fun main(args: Array<String>) {
    Application.launch(JsonPropertiesEditorTestApp::class.java, *args)
}

class JsonPropertiesEditorTestApp : Application() {
    override fun start(primaryStage: Stage) {
        val customResolutionScopeProvider = object : ResolutionScopeProvider {
            override fun getResolutionScopeForElement(objId: String): URI? =
                this::class.java.classLoader.getResource("")?.toURI()
        }

        val viewOptions = ViewOptions(
            markRequired = true,
            groupBy = PropertyGrouping.NONE,
            idRefDisplayMode = IdRefDisplayMode.DESCRIPTION_ONLY,
            numberOfInitiallyOpenedObjects = 2
        )

        val propEdit = JsonPropertiesEditor(false, viewOptions)
        propEdit.referenceProposalProvider = ReferenceProvider
        propEdit.resolutionScopeProvider = customResolutionScopeProvider


        display(propEdit, "StringSchema.json", JSONObject())

        propEdit.valid.addListener { _, _, new -> println("Is valid: $new") }
        primaryStage.scene = Scene(buildUi(propEdit), 800.0, 800.0)
        primaryStage.show()
    }

    private fun display(editor: JsonPropertiesEditor, schemaName: String, data: JSONObject) {
        editor.display("test", "isRoot 1 2 3 4 5 long text", data, loadSchema(schemaName)) {
            println(it.toString(1))
            editor.lookup("c2")
            it
        }
    }

    private fun loadSchema(schemaName: String) =
        JSONObject(JSONTokener(this::class.java.classLoader.getResourceAsStream(schemaName)))

    private fun buildUi(propEdit: JsonPropertiesEditor): Parent {


        val showStars = CheckBox("Show *")

        val groupBy = ComboBox<PropertyGrouping>().apply {
            items.addAll(PropertyGrouping.values())
            selectionModel.select(0)
        }

        val updateViewOptions = { _: Any? ->
            val newViewOptions =
                ViewOptions(showStars.isSelected, groupBy.selectionModel.selectedItem)

            propEdit.viewOptions = newViewOptions
        }

        showStars.selectedProperty().addListener(updateViewOptions)

        groupBy.selectionModel.selectedItemProperty().addListener(updateViewOptions)

        return VBox(
            HBox(
                showStars,
                groupBy
            ),
            PropertiesEditorToolbar(propEdit).node,
            propEdit
        )
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
                    JSONObject(
                        mapOf(
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
                        )
                    ),
                    JSONObject(JSONTokener(this::class.java.classLoader.getResourceAsStream("TestSchema2.json")))
                )
            )

        override fun calcCompletionProposals(
            part: String?,
            editedElement: String,
            editedSchema: StringSchema,
            idRefMode: IdRefDisplayMode
        ): Stream<String> =
            if (idRefMode == IdRefDisplayMode.DESCRIPTION_WITH_ID || idRefMode == IdRefDisplayMode.DESCRIPTION_ONLY) possibleProposals.keys.stream()
            else possibleProposals.keys.stream().filter { it.startsWith(part ?: "") }


        override fun getReferenceDescription(
            reference: String?,
            editedElement: String,
            editedSchema: StringSchema
        ): String =
            possibleProposals[reference]?.data?.optString("name") ?: ""

        override fun isValidReference(
            userInput: String?,
            editedElement: String,
            editedSchema: StringSchema
        ): Boolean =
            possibleProposals.contains(userInput)

        override fun getDataAndSchema(id: String): IdReferenceProposalProvider.DataWithSchema? =
            possibleProposals[id]

        override fun isOpenable(id: String) = true
        override fun openElement(id: String) {
            println("Request to open $id")
        }
    }
}
