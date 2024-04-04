package com.github.hanseter.json.editor.controls

import javafx.beans.property.Property
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextArea
import javafx.scene.control.skin.TextAreaSkin

class MultiLineStringControl : ControlWithProperty<String?> {

    private var scrollBarPolicy: ScrollPane.ScrollBarPolicy = ScrollPane.ScrollBarPolicy.NEVER

    override val control: TextArea = TextArea().apply {
        maxHeight = 26.0
        prefHeightProperty().bind(maxHeightProperty())
        minHeightProperty().bind(maxHeightProperty())
        textProperty().addListener { _, _, text ->
            if ('\n' in text) {
                maxHeight = 70.0
                setHorizonzalScrollbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED)
            } else {
                maxHeight = 26.0
                setHorizonzalScrollbarPolicy(ScrollPane.ScrollBarPolicy.NEVER)
            }
        }
        skinProperty().addListener { _, _, new ->
            if (new != null) {
                setHorizonzalScrollbarPolicy(scrollBarPolicy)
            }
        }
    }

    private fun TextArea.setHorizonzalScrollbarPolicy(policy: ScrollPane.ScrollBarPolicy) {
        scrollBarPolicy = policy
        ((skin as? TextAreaSkin)?.children?.single() as? ScrollPane)?.hbarPolicy = policy
    }

    override val property: Property<String?>
        get() = control.textProperty()


    override fun previewNull(b: Boolean) {
        control.promptText = if (b) TypeControl.NULL_PROMPT else ""
    }
}