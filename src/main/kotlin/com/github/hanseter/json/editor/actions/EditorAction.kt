package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.PropertiesEditInput
import com.github.hanseter.json.editor.PropertiesEditResult
import com.github.hanseter.json.editor.types.TypeModel
import javafx.event.Event

/**
 *
 */
interface EditorAction {

    val text: String

    val description: String

    val selector: TargetSelector

    fun apply(input: PropertiesEditInput, model: TypeModel<*, *>, mouseEvent: Event?): PropertiesEditResult?

    fun shouldBeDisabled(model: TypeModel<*, *>, objId: String): Boolean =
            model.schema.readOnly
}