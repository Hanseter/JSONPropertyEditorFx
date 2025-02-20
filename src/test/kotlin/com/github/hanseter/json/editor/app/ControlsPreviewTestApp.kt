package com.github.hanseter.json.editor.app

import com.github.hanseter.json.editor.ControlFactory
import com.github.hanseter.json.editor.IdReferenceProposalProvider
import com.github.hanseter.json.editor.JsonPropertiesEditor
import com.github.hanseter.json.editor.SchemaNormalizer
import com.github.hanseter.json.editor.base.TestUtils
import com.github.hanseter.json.editor.controls.TypeControl
import com.github.hanseter.json.editor.extensions.SimpleEffectiveSchema
import com.github.hanseter.json.editor.util.*
import javafx.application.Application
import javafx.application.Application.launch
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.SplitPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.json.JSONObject


fun main(args: Array<String>) {
    launch(ControlsPreviewTestApp::class.java, *args)
}

class ControlsPreviewTestApp : Application() {

    private val objId = "test"

    private val previewContainer = GridPane().apply {
        hgap = 10.0
        vgap = 10.0
    }

    private val viewOptions = ViewOptions(
        markRequired = true,
        groupBy = PropertyGrouping.NONE,
        idRefDisplayMode = IdRefDisplayMode.ID_WITH_DESCRIPTION,
        numberOfInitiallyOpenedObjects = 2
    )


    override fun start(primaryStage: Stage) {

        val root = BorderPane()


        val propEdit = JsonPropertiesEditor(false, viewOptions)

        val schemas = TestUtils.createSchemaComboBox().apply {
            this.valueProperty().addListener { _, _, newValue ->
                if (newValue != null) {
                    display(propEdit, newValue, JSONObject())
                    updatePreview(newValue, JSONObject(), viewOptions)
                }
            }
            selectionModel.selectFirst()
        }

        root.top = HBox(5.0, schemas, TestUtils.createThemeComboBox())

        root.center = SplitPane(propEdit, VBox(10.0).apply {
            children.setAll(
                Label("Preview"),
                ScrollPane(previewContainer)
            )
        })

        primaryStage.scene = Scene(root, 1000.0, 800.0)
        primaryStage.show()
    }

    private fun updatePreview(schemaName: String, data: JSONObject, viewOptions: ViewOptions) {

        var schema = SimpleEffectiveSchema(
            null,
            SchemaNormalizer.parseSchema(TestUtils.loadSchema(schemaName), null, false),
        )
        val objectControl = ControlFactory.convert(
            schema,
            EditorContext(
                { IdReferenceProposalProvider.IdReferenceProposalProviderEmpty },
                objId,
                {},
                viewOptions.idRefDisplayMode,
                viewOptions.decimalFormatSymbols,
            )
        )
        objectControl.bindTo(RootBindableType(data))
        val childs = findAllChildControls(objectControl)
        previewContainer.children.clear()
        childs.forEachIndexed { index, it ->
            previewContainer.add(Label(it.model.schema.propertyName), 0, index)
            previewContainer.add(Label(it.model.previewString.string), 1, index)
            if (it.model.previewString.isPseudoValue) previewContainer.add(
                Label("pseudo"),
                2,
                index
            )
            if (it.model.previewString.isDefaultValue) previewContainer.add(
                Label("default"),
                3,
                index
            )
        }
    }


    private fun findAllChildControls(
        objectControl: TypeControl,
        list: MutableList<TypeControl> = mutableListOf()
    ): List<TypeControl> {
        list.add(objectControl)
        objectControl.childControls.forEach {
            findAllChildControls(it, list)
        }
        return list
    }

    private fun display(editor: JsonPropertiesEditor, schemaName: String, data: JSONObject) {
        editor.clear()
        editor.display(objId, "title", data, TestUtils.loadSchema(schemaName)) {
            updatePreview(schemaName, data, viewOptions)
            println(it.toString(2))
            it
        }
    }
}