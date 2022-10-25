package com.github.hanseter.json.editor.controls

import javafx.beans.property.Property
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.util.converter.IntegerStringConverter
import org.everit.json.schema.NumberSchema

class IntegerControl : NumberControl<Int?>() {

    override val control: Spinner<Int?> =
        Spinner<Int?>(IntegerSpinnerValueFactoryNullSafe()).apply {
            isEditable = true
        }

    init {
        initControl()
    }

    class IntegerSpinnerValueFactoryNullSafe : SpinnerValueFactory<Int?>() {
        init {
            converter = IntegerStringConverter()
        }

        override fun increment(steps: Int) {
            value = value?.plus(steps)
        }

        override fun decrement(steps: Int) {
            value = value?.minus(steps)
        }
    }
}