package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.types.TypeModel
import com.github.hanseter.json.editor.util.BindableJsonType
import javafx.scene.Node

interface TypeControl {
    val control: Node?
    val model: TypeModel<*, *>
    val childControls: List<TypeControl>
    fun bindTo(type: BindableJsonType)

    companion object {
        const val NULL_PROMPT = "Null"
    }
}