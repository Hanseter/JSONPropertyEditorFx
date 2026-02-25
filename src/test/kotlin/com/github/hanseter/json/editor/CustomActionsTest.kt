package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.actions.EditorAction
import com.github.hanseter.json.editor.actions.TargetSelector
import com.github.hanseter.json.editor.base.TestUtils
import com.github.hanseter.json.editor.types.TypeModel
import javafx.event.Event
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.ButtonBase
import javafx.stage.Stage
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start

@ExtendWith(ApplicationExtension::class)
class CustomActionsTest {

    lateinit var editor: JsonPropertiesEditor

    @Start
    fun start(stage: Stage) {
        editor = JsonPropertiesEditor()
    }

    @Test
    fun `initially registered custom action is accessible`() {
        val customAction = TestEditorAction()

        val editor = JsonPropertiesEditor(
            actions = listOf(customAction)
        )

        val schema = JSONObject("""{"type":"object","properties":{"bool":{"type":"boolean"}}}""")
        editor.display("1", "1", JSONObject().put("bool", true), schema) { it }

        val cell = editor.getActionCellInTable("bool")

        MatcherAssert.assertThat((cell.graphic as Parent).childrenUnmodifiable, Matchers.hasSize(1))

        val child = (cell.graphic as Parent).childrenUnmodifiable.single() as ButtonBase

        TestUtils.waitForAsyncFx {
            child.fire()
        }

        MatcherAssert.assertThat(customAction.useCounter, Matchers.`is`(1))
    }

    @Test
    fun `action can be removed`() {
        val customAction = TestEditorAction()

        val editor = JsonPropertiesEditor(
            actions = listOf(customAction)
        )

        val schema = JSONObject("""{"type":"object","properties":{"bool":{"type":"boolean"}}}""")
        editor.display("1", "1", JSONObject().put("bool", true), schema) { it }


        var cell = editor.getActionCellInTable("bool")

        MatcherAssert.assertThat((cell.graphic as Parent).childrenUnmodifiable, Matchers.hasSize(1))

        editor.customActions = emptyList()

        cell = editor.getActionCellInTable("bool")

        MatcherAssert.assertThat((cell.graphic as Parent).childrenUnmodifiable, Matchers.empty())
    }

    @Test
    fun `action can be added`() {
        val customAction = TestEditorAction()

        val editor = JsonPropertiesEditor(
            actions = emptyList()
        )

        val schema = JSONObject("""{"type":"object","properties":{"bool":{"type":"boolean"}}}""")
        editor.display("1", "1", JSONObject().put("bool", true), schema) { it }


        var cell = editor.getActionCellInTable("bool")

        MatcherAssert.assertThat((cell.graphic as Parent).childrenUnmodifiable, Matchers.empty())


        editor.customActions = listOf(customAction)

        cell = editor.getActionCellInTable("bool")

        MatcherAssert.assertThat((cell.graphic as Parent).childrenUnmodifiable, Matchers.hasSize(1))

        val child = (cell.graphic as Parent).childrenUnmodifiable.single() as ButtonBase

        TestUtils.waitForAsyncFx {
            child.fire()
        }

        MatcherAssert.assertThat(customAction.useCounter, Matchers.`is`(1))
    }

    inner class TestEditorAction : EditorAction {

        var useCounter = 0

        override fun createIcon(size: Int): Node {
            return EditorAction.createTextIcon("A", size)
        }

        override val description: String
            get() = "Test Action"

        override val selector: TargetSelector
            get() = TargetSelector.Always

        override fun apply(
            input: PropertiesEditInput,
            model: TypeModel<*, *>,
            mouseEvent: Event?
        ): PropertiesEditResult? {
            useCounter++
            return null
        }

    }

}