package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.types.TypeModel
import javafx.event.Event
import org.json.JSONObject

/**
 *
 */
interface EditorAction {

    val text: String

    val description: String

    val selector: TargetSelector

    fun apply(currentData: JSONObject, model: TypeModel<*, *>, mouseEvent: Event?): JSONObject?

    fun shouldBeDisabled(model: TypeModel<*, *>, objId: String): Boolean =
            model.schema.readOnly
}