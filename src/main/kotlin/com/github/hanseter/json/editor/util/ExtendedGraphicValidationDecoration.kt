package com.github.hanseter.json.editor.util

import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import org.controlsfx.validation.Severity
import org.controlsfx.validation.decoration.GraphicValidationDecoration

/**
 * Extension to the [GraphicValidationDecoration] to make it support [Severity.OK] (by default, it
 * is treated as if it were [Severity.INFO]).
 */
class ExtendedGraphicValidationDecoration : GraphicValidationDecoration() {


    override fun getStyleBySeverity(severity: Severity?): String {
        if (severity == Severity.OK) {
            return OK_TOOLTIP_EFFECT
        }
        return super.getStyleBySeverity(severity)
    }

    override fun getGraphicBySeverity(severity: Severity?): Node {
        if (severity == Severity.OK) {
            return ImageView(OK_IMAGE)
        }
        return super.getGraphicBySeverity(severity)
    }

    companion object {

        private val OK_IMAGE: Image = Image(
            ExtendedGraphicValidationDecoration::class.java.getResource("decoration-ok.png")!!
                .toExternalForm()
        )
        
        private const val POPUP_SHADOW_EFFECT =
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 5, 0, 0, 5);"

        private const val TOOLTIP_COMMON_EFFECTS =
            "-fx-font-weight: bold; -fx-padding: 5; -fx-border-width:1;"

        private val OK_TOOLTIP_EFFECT = (POPUP_SHADOW_EFFECT + TOOLTIP_COMMON_EFFECTS
                + "-fx-background-color: #EBF4ED; -fx-text-fill: #007906; -fx-border-color: #007906;")

    }

}