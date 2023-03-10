package com.github.hanseter.json.editor.i18n

import java.util.*

object JsonPropertiesMl {
    val bundle = ResourceBundle.getBundle("ml_jsonEditor")

    fun validatorSubErrors(count: Int): String =
        if (count == 1) bundle.getString("jsonEditor.validators.subError").format(1)
        else bundle.getString("jsonEditor.validators.subErrors").format(count)

}