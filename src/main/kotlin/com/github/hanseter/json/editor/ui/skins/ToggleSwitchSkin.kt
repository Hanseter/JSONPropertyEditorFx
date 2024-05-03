package com.github.hanseter.json.editor.ui.skins

import com.sun.javafx.scene.control.behavior.ButtonBehavior
import javafx.animation.Animation
import javafx.animation.TranslateTransition
import javafx.beans.binding.Bindings
import javafx.beans.value.ChangeListener
import javafx.collections.ListChangeListener
import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.control.CheckBox
import javafx.scene.control.skin.LabeledSkinBase
import javafx.scene.layout.StackPane
import javafx.util.Duration
import kotlin.math.max
import kotlin.math.min

class ToggleSwitchSkin(checkBox: CheckBox) : LabeledSkinBase<CheckBox>(checkBox) {

    companion object {
        private val CSS =
            ToggleSwitchSkin::class.java.getResource("toggleSwitch.css")!!.toExternalForm()
    }

    private val behavior = ButtonBehavior(checkBox)


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


    private val selectionListener = ChangeListener<Boolean> { _, old, new ->
        if (new != old) {
            selectedStateChanged()
        }
    }

    init {
        checkBox.stylesheets.setAll(CSS)
        children.add(box)
        checkBox.selectedProperty().addListener(selectionListener)
    }

    override fun dispose() {
        super.dispose()
        skinnable.selectedProperty().removeListener(selectionListener)
        behavior.dispose()
    }

    private fun selectedStateChanged() {
        // Stop the transition if it was already running, has no effect otherwise.
        transition.stop()

        transition.rate = if (skinnable.isSelected) 1.0 else -1.0

        // we jump the selection to the start based on the current direction (-duration will be coerced to 0)
        transition.jumpTo(transition.duration.multiply(-transition.rate))

        if (skinnable.isArmed || skinnable.isFocused) {
            transition.play()
        } else {
            skinnable.requestLayout()
        }

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
        ) + snapSizeX(
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
        return max(
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
        ) + snapSizeX(
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
        return max(
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
        val boxWidth = snapSizeX(box.prefWidth(-1.0))
        val boxHeight = snapSizeY(box.prefHeight(-1.0))
        val computeWidth = max(checkBox.prefWidth(-1.0), checkBox.minWidth(-1.0))
        val labelWidth = min(computeWidth - boxWidth, w - snapSizeX(boxWidth))
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

    override fun updateChildren() {
        super.updateChildren()
        // This method is invoked during the super constructor (among others),
        // where `box` is not initialized yet.
        // This implementation is identical to the one in CheckBoxSkin, which is in the same situation.
        @Suppress("SENSELESS_COMPARISON")
        if (box != null) {
            children.add(box)
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