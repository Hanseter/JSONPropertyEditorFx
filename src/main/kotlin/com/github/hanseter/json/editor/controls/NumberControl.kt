/*
 * SWEETS - Software Engineering Tooling Suite
 *
 * Copyright (c) Siemens Mobility GmbH 2022, All Rights Reserved, Confidential.
 */
package com.github.hanseter.json.editor.controls

import javafx.application.Platform
import javafx.beans.property.Property
import javafx.scene.control.Spinner

/**
 *
 * @author Henrik Frühling (henrik.fruehling@siemens.com)
 */
abstract class NumberControl<T : Number?> : ControlWithProperty<T> {

    abstract override val control: Spinner<T>

    override val property: Property<T>
    get() = control.valueFactory.valueProperty()

    protected fun initControl(){
        control.focusedProperty().addListener { _, _, new ->
            if (!new && (control.editor.text.isEmpty() || control.editor.text == "-")) {
                control.editor.text =
                    control.valueFactory.converter.toString(property.value)
            }
        }
        control.editor.textProperty().addListener { _, _, _ ->
            updateValueAfterTextChange(control)
        }
    }

    override fun previewNull(b: Boolean) {
        control.editor.promptText = if (b) TypeControl.NULL_PROMPT else ""
    }

    companion object {
        fun <T> updateValueAfterTextChange(control: Spinner<T>) {
            Platform.runLater {
                val new = control.editor.text
                if (new.isNotEmpty() && new != "-") {
                    try {
                        val caretPos = control.editor.caretPosition
                        control.increment(0) // won't change value, but will commit editor
                        control.editor.text = new
                        control.editor.positionCaret(caretPos)
                    } catch (e: NumberFormatException) {
                        control.editor.text =
                            control.valueFactory.converter.toString(control.valueFactory.value)
                                ?: "0"
                    }
                }
            }
        }
    }
}