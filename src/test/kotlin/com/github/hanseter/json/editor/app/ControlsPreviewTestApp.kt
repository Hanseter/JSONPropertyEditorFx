/*
 * SWEETS - Software Engineering Tooling Suite
 *
 * Copyright (c) Siemens Mobility GmbH 2022, All Rights Reserved, Confidential.
 */
package com.github.hanseter.json.editor.app

import com.github.hanseter.json.editor.JsonPropertiesEditor
import com.github.hanseter.json.editor.base.TestUtils
import com.github.hanseter.json.editor.util.IdRefDisplayMode
import com.github.hanseter.json.editor.util.PropertyGrouping
import com.github.hanseter.json.editor.util.ViewOptions
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.json.JSONObject

/**
 *
 * @author Henrik Frühling (henrik.fruehling@siemens.com)
 */
class ControlsPreviewTestApp : Application() {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(ControlsPreviewTestApp::class.java, *args)
        }
    }

    override fun start(primaryStage: Stage) {

        val root = BorderPane()

        root.right = VBox(10.0).apply {
            children.setAll(
                Label("Preview"),
                Button("Hi")
            )
        }

        val viewOptions = ViewOptions(
            markRequired = true,
            groupBy = PropertyGrouping.NONE,
            idRefDisplayMode = IdRefDisplayMode.ID_WITH_DESCRIPTION,
            numberOfInitiallyOpenedObjects = 2
        )

        val propEdit = JsonPropertiesEditor(false, viewOptions)

        val schemas = TestUtils.createSchemaComboBox().apply {
            this.valueProperty().addListener { _, _, newValue ->
                if (newValue != null) {
                    display(propEdit, newValue, JSONObject())
                    updatePreview()
                }
            }
            selectionModel.selectFirst()
        }

        root.top = schemas
        root.center = propEdit

        primaryStage.scene = Scene(root, 800.0, 800.0)
        primaryStage.show()
    }

    private fun updatePreview() {

    }

    private fun display(editor: JsonPropertiesEditor, schemaName: String, data: JSONObject) {
        editor.clear()
        editor.display("test", "title", data, TestUtils.loadSchema(schemaName)) {
            it
        }
    }
}