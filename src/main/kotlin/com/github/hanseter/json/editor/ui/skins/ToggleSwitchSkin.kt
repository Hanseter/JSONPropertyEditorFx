package com.github.hanseter.json.editor.ui.skins

import com.sun.javafx.scene.control.behavior.ButtonBehavior
import com.sun.javafx.scene.control.skin.LabeledSkinBase
import javafx.animation.Animation
import javafx.animation.TranslateTransition
import javafx.beans.binding.Bindings
import javafx.css.CssMetaData
import javafx.css.Styleable
import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.control.CheckBox
import javafx.scene.layout.StackPane
import javafx.util.Duration
import kotlin.math.max
import kotlin.math.min

class ToggleSwitchSkin(checkBox: CheckBox) :
    LabeledSkinBase<CheckBox, ButtonBehavior<CheckBox>>(checkBox, ButtonBehavior(checkBox)) {

    companion object {
        private val CSS =
            ToggleSwitchSkin::class.java.getResource("toggleSwitch.css")!!.toExternalForm()
    }

    init {
        checkBox.stylesheets.setAll(CSS)
    }

    private val thumb = StackPane()
    private val thumbArea = StackPane()
    private val transition = TranslateTransition(Duration.millis(200.0), thumb)

    private val box = StackPane().apply {
        thumbArea.apply {
            styleClass.setAll("thumb-area")
        }
        thumb.apply {
            styleClass.setAll("thumb")
            maxWidthProperty().bind(Bindings.divide(thumbArea.widthProperty(), 2.0))
        }
        children.addAll(thumbArea, thumb)
    }


    init {
        children.add(box)
        checkBox.selectedProperty().addListener { _, oldValue, newValue ->
            if (newValue != oldValue) {
                selectedStateChanged()
            }
        }
    }

    private fun selectedStateChanged() {
        // Stop the transition if it was already running, has no effect otherwise.
        transition.stop()
        if (skinnable.isSelected) {
            transition.rate = 1.0
            transition.jumpTo(Duration.ZERO)
        } else {
            // If we are not selected, we need to go from right to left.
            transition.rate = -1.0
            transition.jumpTo(transition.duration)
        }
        transition.play()
    }

    override fun computeMinWidth(
        height: Double,
        topInset: Double,
        rightInset: Double,
        bottomInset: Double,
        leftInset: Double
    ): Double {
        return super.computeMinWidth(
            height,
            topInset,
            rightInset,
            bottomInset,
            leftInset
        ) + snapSize(
            box.minWidth(-1.0)
        )
    }

    override fun computeMinHeight(
        width: Double,
        topInset: Double,
        rightInset: Double,
        bottomInset: Double,
        leftInset: Double
    ): Double {
        return Math.max(
            super.computeMinHeight(
                width - box.minWidth(-1.0),
                topInset,
                rightInset,
                bottomInset,
                leftInset
            ),
            topInset + box.minHeight(-1.0) + bottomInset
        )
    }

    override fun computePrefWidth(
        height: Double,
        topInset: Double,
        rightInset: Double,
        bottomInset: Double,
        leftInset: Double
    ): Double {
        return super.computePrefWidth(
            height,
            topInset,
            rightInset,
            bottomInset,
            leftInset
        ) + snapSize(
            box.prefWidth(-1.0)
        )
    }

    override fun computePrefHeight(
        width: Double,
        topInset: Double,
        rightInset: Double,
        bottomInset: Double,
        leftInset: Double
    ): Double {
        return Math.max(
            super.computePrefHeight(
                width - box.prefWidth(-1.0),
                topInset,
                rightInset,
                bottomInset,
                leftInset
            ),
            topInset + box.prefHeight(-1.0) + bottomInset
        )
    }

    override fun layoutChildren(
        x: Double, y: Double,
        w: Double, h: Double
    ) {
        val checkBox = skinnable
        val boxWidth = snapSize(box.prefWidth(-1.0))
        val boxHeight = snapSize(box.prefHeight(-1.0))
        val computeWidth = max(checkBox.prefWidth(-1.0), checkBox.minWidth(-1.0))
        val labelWidth = min(computeWidth - boxWidth, w - snapSize(boxWidth))
        val labelHeight = min(checkBox.prefHeight(labelWidth), h)
        val maxHeight = max(boxHeight, labelHeight)
        val xOffset = computeXOffset(w, labelWidth + boxWidth, checkBox.alignment.hpos) + x
        val yOffset = computeYOffset(h, maxHeight, checkBox.alignment.vpos) + x
        layoutLabelInArea(xOffset + boxWidth, yOffset, labelWidth, maxHeight, checkBox.alignment)

        box.resize(boxWidth, boxHeight)

        // Each time the layout is done, recompute the thumb "selected" position and apply it to the transition target.
        val transitionDist = boxWidth / 4
        transition.fromX = -transitionDist
        transition.toX = transitionDist

        thumb.translateX = 0.0

        if (transition.status == Animation.Status.RUNNING) {
            // If the transition is running, it must be restarted for the value to be properly updated.
            val currentTime = transition.currentTime
            transition.stop()
            transition.playFrom(currentTime)
        } else {
            // If the transition is not running, simply apply the translate value.
            thumb.translateX = when {
                !checkBox.isSelected -> transition.fromX
                checkBox.isSelected -> transition.toX
                checkBox.isIndeterminate -> 0.0
                else -> 0.0
            }
        }
    }


    private fun computeYOffset(h: Double, contentHeight: Double, vpos: VPos): Double {
        return when (vpos) {
            VPos.TOP -> 0.0
            VPos.CENTER -> (h - contentHeight) / 2
            VPos.BOTTOM -> h - contentHeight
            else -> 0.0
        }

    }

    private fun computeXOffset(w: Double, contentWidth: Double, hpos: HPos?): Double {
        return when (hpos) {
            HPos.LEFT -> 0.0
            HPos.CENTER -> (w - contentWidth) / 2
            HPos.RIGHT -> w - contentWidth
            else -> 0.0
        }
    }
}