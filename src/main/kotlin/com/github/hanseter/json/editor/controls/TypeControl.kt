package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.types.PreviewString
import com.github.hanseter.json.editor.types.TypeModel
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.LazyControl

interface TypeControl {
    val model: TypeModel<*, *>
    val childControls: List<TypeControl>
    fun bindTo(type: BindableJsonType)

    fun createLazyControl(): LazyControl

    val previewString: PreviewString

    companion object {
        const val NULL_PROMPT = "Null"
    }
}