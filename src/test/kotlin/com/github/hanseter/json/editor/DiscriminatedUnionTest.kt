package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.types.DiscriminatedOneOfModel
import com.github.hanseter.json.editor.ui.ControlTreeItemData
import javafx.scene.control.ComboBox
import javafx.stage.Stage
import org.everit.json.schema.Schema
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start

@ExtendWith(ApplicationExtension::class)
class DiscriminatedUnionTest {

    lateinit var editor: JsonPropertiesEditor

    @Start
    fun start(stage: Stage) {
        editor = JsonPropertiesEditor()
    }

    @Test
    fun usesDiscUnion() {

        val schema = getDiscUnionSchema("a", "b")

        editor.display("1", "1", JSONObject(), schema) { it }
        val itemTable = editor.getItemTable()
        val item = itemTable.root.children.first().findChildWithKey("union")!!
        val itemData = item.value as ControlTreeItemData

        assertThat(itemData.typeControl.model, instanceOf(DiscriminatedOneOfModel::class.java))
        assertThat(
            (itemData.typeControl.model as DiscriminatedOneOfModel).discriminatorKey,
            `is`("disc")
        )
    }

    @Test
    fun usesCorrectOption() {
        val schema = getDiscUnionSchema("A", "B", "C")

        editor.display(
            "1", "1", JSONObject(
                mapOf(
                    "union" to mapOf(
                        "disc" to "B",
                        // deliberately using a different value prop to see if the discrimator overrules it
                        "valueA" to "foo"
                    )
                )
            ), schema
        ) { it }

        val control = editor.getControlInTable("union") as ComboBox<Schema>

        assertThat(control.selectionModel.selectedIndex, `is`(1))
    }

    @Test
    fun doesntUseDiscUnion() {


        val schema = JSONObject(
            mapOf(
                "properties" to mapOf(
                    "noDisc" to mapOf(
                        "oneOf" to listOf(
                            generateOption("A", null),
                            generateOption("B", null)
                        )
                    ),
                    "notRequired" to mapOf(
                        "oneOf" to listOf(
                            generateOption("A", optionalDisc = false),
                            generateOption("B", optionalDisc = true)
                        )
                    ),
                    "differentDiscs" to mapOf(
                        "oneOf" to listOf(
                            generateOption("A", discKey = "disc"),
                            generateOption("B", discKey = "different")
                        )
                    )
                )
            )
        )

        editor.display("1", "1", JSONObject(), schema) { it }
        val itemTable = editor.getItemTable()

        for (key in listOf("noDisc", "notRequired", "differentDiscs")) {
            val item = itemTable.root.children.first().findChildWithKey(key)!!
            val itemData = item.value as ControlTreeItemData

            assertThat(
                itemData.typeControl.model,
                not(instanceOf(DiscriminatedOneOfModel::class.java))
            )
        }
    }

    @Test
    fun setsDiscriminatorWhenSwitching() {
        val schema = getDiscUnionSchema("D", "E", "F")

        val data = JSONObject()

        editor.display("1", "1", data, schema) { it }


        val itemTable = editor.getItemTable()
        val item = itemTable.root.children.first().findChildWithKey("union")!!
        val control = editor.getControlInTable("union") as ComboBox<Schema>
        control.selectionModel.selectFirst()

        assertThat(data.optJSONObject("union")?.opt("disc"), `is`("D"))

        control.selectionModel.select(2)

        assertThat(data.optJSONObject("union")?.opt("disc"), `is`("F"))
    }


    companion object {

        fun generateOption(
            identifier: String,
            discKey: String? = "disc",
            optionalDisc: Boolean = false
        ): Map<String, Any> {

            val props = mutableMapOf(
                "value$identifier" to mapOf(
                    "type" to "string"
                )
            )

            discKey?.let {
                props.put(it, mapOf("const" to identifier))
            }

            return mapOf(
                "type" to "object",
                "title" to identifier,
                "properties" to props,
                "required" to listOfNotNull(if (optionalDisc) null else discKey, "value$identifier")
            )
        }

        fun getDiscUnionSchema(vararg options: String): JSONObject {

            return JSONObject(
                mapOf(
                    "properties" to mapOf(
                        "union" to mapOf(
                            "oneOf" to options.map { generateOption(it) }
                        )
                    )
                )
            )
        }

    }

}