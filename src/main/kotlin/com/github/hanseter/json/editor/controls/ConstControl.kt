package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import javafx.beans.binding.Bindings
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Label

class ConstControl : ControlWithProperty<Any?> {

    override val property: Property<Any?> = SimpleObjectProperty(null)

    override val control = Label(JsonPropertiesMl.bundle.getString("jsonEditor.control.constControl")).apply {
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