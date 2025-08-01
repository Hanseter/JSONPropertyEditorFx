package com.github.hanseter.json.editor.app

import com.github.hanseter.json.editor.*
import com.github.hanseter.json.editor.util.IdRefDisplayMode
import com.github.hanseter.json.editor.util.PropertyGrouping
import com.github.hanseter.json.editor.util.ViewOptions
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import org.intellij.lang.annotations.Language
import org.json.JSONObject
import java.net.URI

fun main(args: Array<String>) {
    Application.launch(SchemaChangingTestApp::class.java, *args)
}

class SchemaChangingTestApp : Application() {

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
        propEdit.referenceProposalProvider = JsonPropertiesEditorTestApp.ReferenceProvider
        propEdit.resolutionScopeProvider = customResolutionScopeProvider

        @Language("JSON")
        val schemaString = """
{
  "type": "object",
  "properties": {
    "a": {
      "enum": ["string", "number"],
      "default": "string"
    },
    "b": {
      "type": "string",
      "style": "-fx-background-color: yellow;"
    },
    "arr": {
      "type": "array",
      "items": {
        "type": "boolean"
      }
    },
    "obj": {
      "type": "object",
      "properties": {
        "a": {
          "type": "boolean"
        },
        "subObj": {
          "type": "object",
          "properties": {
            "a": {
              "type": "string"
            },
            "b": {
              "type": "number"
            }
          }
        }
      }
    }
  }
}
"""

        var schema = JSONObject(schemaString)


        val callback: (PropertiesEditInput) -> PropertiesEditResult = {

            val newSchema = it.schema.raw

            val bSchema = newSchema.getJSONObject("properties").getJSONObject("b")

            if (it.data.optString("a", "string") == "number") {
                bSchema.put("type", "number")
            } else {
                bSchema.put("type", "string")
            }

            schema = newSchema

            PropertiesEditResult(
                SchemaNormalizer.deepCopy(it.data).put("b", ""),
                ParsedSchema.Companion.create(newSchema, null as URI)
            )
        }

        propEdit.display(
            "test", "Test Object", JSONObject(), schema, callback
        )

        propEdit.valid.addListener { _, _, new -> println("Is valid: $new") }
        primaryStage.scene = Scene(propEdit, 800.0, 800.0)
        primaryStage.show()
    }
}