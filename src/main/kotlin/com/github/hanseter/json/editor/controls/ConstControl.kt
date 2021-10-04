package com.github.hanseter.json.editor.controls

import javafx.beans.binding.Bindings
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Label

class ConstControl : ControlWithProperty<Any?> {

    override val property: Property<Any?> = SimpleObjectProperty(null)

    override val control = Label("Const value").apply {
        textProperty().bind(
            Bindings.createStringBinding(
                { property.value?.toString() ?: "null" },
                property
            )
        )
    }


    override fun previewNull(b: Boolean) {
        property.value = null
    }
}