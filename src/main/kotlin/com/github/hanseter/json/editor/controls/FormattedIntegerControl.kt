package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.types.FormattedIntegerModel
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import org.everit.json.schema.NumberSchema
import java.text.DecimalFormatSymbols

class FormattedIntegerControl(
    model: FormattedIntegerModel
) : NumberControl<Int?>() {

    private val intFormat = model.intFormat

    override val control: Spinner<Int?> =
        Spinner<Int?>(FormattedIntegerSpinnerValueFactoryNullSafe(intFormat)).apply {
            isEditable = true
            valueFactory.value = null
        }

    init {
        initControl()
        control.focusedProperty().addListener { _, _, new ->
            //apply formatted value on focus lost
            if (!new) {

                val formatted = control.valueFactory.converter.toString(control.value)
                control.editor.text = formatted
            }
        }
    }

    private class FormattedIntegerSpinnerValueFactoryNullSafe(private val intFormat: FormattedIntegerModel.IntFormat) :
        SpinnerValueFactory<Int?>() {

        init {
            converter = intFormat.converter
        }

        override fun increment(steps: Int) {
            value = value?.plus((steps * intFormat.multiplier).toInt())
        }

        override fun decrement(steps: Int) {
            value = value?.minus((steps * intFormat.multiplier).toInt())
        }
    }

    override fun previewNull(b: Boolean) {
        control.editor.promptText = if (b) TypeControl.NULL_PROMPT else ""
    }
}


