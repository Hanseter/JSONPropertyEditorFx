package com.github.hanseter.json.editor.app

import com.github.hanseter.json.editor.*
import com.github.hanseter.json.editor.base.TestUtils
import com.github.hanseter.json.editor.base.TestUtils.loadSchema
import com.github.hanseter.json.editor.types.TypeModel
import com.github.hanseter.json.editor.ui.PropertiesEditorToolbar
import com.github.hanseter.json.editor.util.CustomizationObject
import com.github.hanseter.json.editor.util.IdRefDisplayMode
import com.github.hanseter.json.editor.util.PropertyGrouping
import com.github.hanseter.json.editor.util.ViewOptions
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
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
import kotlin.math.exp

fun main(args: Array<String>) {
    Application.launch(JsonPropertiesEditorTestApp::class.java, *args)
}

const val TEST_ELEMENT_COUNT = 10

class JsonPropertiesEditorTestApp : Application() {
    private val customResolutionScopeProvider = object : ResolutionScopeProvider {
        override fun getResolutionScopeForElement(objId: String): URI? =
            this::class.java.classLoader.getResource("")?.toURI()
    }

    override fun start(primaryStage: Stage) {

        val viewOptions = ViewOptions(
            markRequired = true,
            groupBy = PropertyGrouping.NONE,
            idRefDisplayMode = IdRefDisplayMode.ID_WITH_DESCRIPTION,
            numberOfInitiallyOpenedObjects = 2,
        )

        val propEdit = JsonPropertiesEditor(
            false,
            viewOptions,
            customizationObject = TestCustomizationObject()
        )
        propEdit.referenceProposalProvider = ReferenceProvider
        propEdit.resolutionScopeProvider = customResolutionScopeProvider
        propEdit.selectionModel.selectedItemProperty().addListener { _,_,selected ->
            println(selected)
        }

        propEdit.valid.addListener { _, _, new -> println("Is valid: $new") }
        primaryStage.scene = Scene(buildUi(propEdit), 800.0, 800.0)
        primaryStage.show()
    }

    private fun display(
        editor: JsonPropertiesEditor,
        schemaName: String,
        data: JSONObject,
        n: Int = TEST_ELEMENT_COUNT
    ) {
        editor.clear()
        val schema = loadSchema(schemaName)
        val parsedSchema = ParsedSchema.create(
            schema,
            customResolutionScopeProvider.getResolutionScopeForElement("")
        )!!
        repeat(n) {
            editor.display(
                "test$it",
                "Element $it",
                SchemaNormalizer.deepCopy(data),
                parsedSchema
            ) {
                println(it.toString(1))
                editor.lookup("c2")
                it
            }
        }

    }


    private fun buildUi(propEdit: JsonPropertiesEditor): Parent {

        val themeComboBox = TestUtils.createThemeComboBox()

        val showStars = CheckBox("Show *")

        val groupBy = ComboBox<PropertyGrouping>().apply {
            items.addAll(PropertyGrouping.entries)
            selectionModel.selectFirst()
        }

        val schemas = TestUtils.createSchemaComboBox().apply {
            this.valueProperty().addListener { _, _, newValue ->
                if (newValue != null) {
                    display(propEdit, newValue, JSONObject())
                }
            }
            selectionModel.selectFirst()
        }

        val updateViewOptions = { _: Any? ->
            val newViewOptions =
                ViewOptions(showStars.isSelected, groupBy.selectionModel.selectedItem)

            propEdit.viewOptions = newViewOptions
        }

        showStars.selectedProperty().addListener(updateViewOptions)

        groupBy.selectionModel.selectedItemProperty().addListener(updateViewOptions)

        val scrollToFirstButton = Button("Scroll to 1.").apply {
            setOnAction {
                propEdit.scrollTo("test0")
            }
        }
        val scrollToFifthButton = Button("Scroll to 5.").apply {
            setOnAction {
                propEdit.selectionModel.select(ElementField("test4", listOf("numbers", "maximum")))
//                propEdit.scrollToField(ElementField("test4", listOf("numbers", "maximum")))
            }
        }
        val add10000ItemsButton = Button("Add").apply {
            setOnAction {
                display(propEdit, schemas.selectionModel.selectedItem, JSONObject(), 10_000)
            }
        }

        val expandAllButton = Button("Expand").apply {
            setOnAction {
                repeat(10_000) {
                    propEdit.expandAll("test$it")
                }
            }
        }
        return VBox(
            HBox(
                showStars,
                groupBy,
                schemas,
                themeComboBox,
                scrollToFirstButton,
                scrollToFifthButton,
                add10000ItemsButton,
                expandAllButton
            ).apply { spacing = 10.0 },
            PropertiesEditorToolbar(propEdit).node,
            propEdit
        ).apply { spacing = 10.0 }
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
                "some/first" to IdReferenceProposalProvider.DataWithSchema(
                    JSONObject().put("name", "1"),
                    JSONObject(JSONTokener(this::class.java.classLoader.getResourceAsStream("TestSchema2.json")))
                ),
                "some/second" to IdReferenceProposalProvider.DataWithSchema(
                    JSONObject().put("name", "2"),
                    JSONObject(JSONTokener(this::class.java.classLoader.getResourceAsStream("TestSchema2.json")))
                ),
                "some/third" to IdReferenceProposalProvider.DataWithSchema(
                    JSONObject().put("name", "3"),
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

class TestCustomizationObject : CustomizationObject {

    override fun getTitle(model: TypeModel<*, *>, defaultTitle: String): String {
        if (model.schema.pointer == listOf("combined object", "b")) {
            return "not b (${model.value})"
        }
        if (model.schema.pointer == listOf("combined object")) {
            return "combined object (${(model.value as? JSONObject)?.optString("a")})"
        }
        return defaultTitle
    }

}
