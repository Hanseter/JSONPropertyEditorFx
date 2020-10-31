package com.github.hanseter.json.editor.controls

import javafx.beans.property.Property
import javafx.scene.control.Control

interface ControlWithProperty<T> {
    val control: Control
    val property: Property<T>

    fun previewNull(b: Boolean)
}