package com.github.hanseter.json.editor.util

import com.github.hanseter.json.editor.validators.Validator
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import org.controlsfx.control.decoration.Decoration
import org.controlsfx.control.decoration.GraphicDecoration
import org.controlsfx.validation.Severity
import org.controlsfx.validation.ValidationMessage
import org.controlsfx.validation.decoration.GraphicValidationDecoration

/**
 * Extension to the [GraphicValidationDecoration] to make it support [Severity.OK] (by default, it
 * is treated as if it were [Severity.INFO]).
 *
 * Also recolors the [Severity.INFO] tooltip because its contrast was so low it was barely legible.
 */
class ExtendedGraphicValidationDecoration : GraphicValidationDecoration() {


    override fun getStyleBySeverity(severity: Severity?): String {
        return when (severity) {
            Severity.OK -> OK_TOOLTIP_EFFECT
            Severity.INFO -> INFO_TOOLTIP_EFFECT
            else -> super.getStyleBySeverity(severity)
        }
    }

    override fun getGraphicBySeverity(severity: Severity?): Node {
        if (severity == Severity.OK) {
            return ImageView(OK_IMAGE)
        }
        return super.getGraphicBySeverity(severity)
    }

    override fun createValidationDecorations(message: ValidationMessage): Collection<Decoration> {
        return listOf<Decoration>(
            GraphicDecoration(
                createDecorationNode(message),
                Pos.BOTTOM_LEFT,
                0.0,
                0.0
            )
        )
    }

    fun applyValidationDecoration(pane: StackPane, result: Validator.ValidationResult) {
        val decoration = getGraphicBySeverity(result.severity).apply {
            Tooltip.install(this, Tooltip(result.message).apply {
                opacity = .9
                isAutoFix = true
                style = getStyleBySeverity(result.severity)
            }
            )
            StackPane.setAlignment(this, Pos.BOTTOM_LEFT)
            StackPane.setMargin(this, Insets(0.0, 0.0, -2.0, -5.0))
            styleClass.add("json-prop-error-decoration-node")
        }
        pane.children.add(decoration)
    }

    fun removeDecorations(pane: StackPane) {
        pane.children.removeAll(
            pane.children.filter { "json-prop-error-decoration-node" in it.styleClass }
        )
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

        private const val OK_TOOLTIP_EFFECT = (POPUP_SHADOW_EFFECT + TOOLTIP_COMMON_EFFECTS
                + "-fx-background-color: #EBF4ED; -fx-text-fill: #007906; -fx-border-color: #007906;")

        private const val INFO_TOOLTIP_EFFECT = (POPUP_SHADOW_EFFECT + TOOLTIP_COMMON_EFFECTS
                + "-fx-background-color: #F0F1FA; -fx-text-fill: #0065DA; -fx-border-color: #0065DA;")
    }

}