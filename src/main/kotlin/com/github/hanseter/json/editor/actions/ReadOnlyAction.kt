package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.controls.TypeControl
import com.github.hanseter.json.editor.extensions.SchemaWrapper

class ReadOnlyAction(text: String, selector: ActionTargetSelector, private val onAction: (schema: SchemaWrapper<*>, value: Any?) -> Unit)
    : EditorAction(text, selector) {

    override fun apply(control: TypeControl) {
        onAction(control.schema, control.getBoundValue())
    }

}