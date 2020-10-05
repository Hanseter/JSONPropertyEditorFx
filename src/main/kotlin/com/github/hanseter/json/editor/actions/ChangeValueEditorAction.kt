package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.controls.TypeControl
import com.github.hanseter.json.editor.extensions.SchemaWrapper

class ChangeValueEditorAction(text: String, selector: ActionTargetSelector, private val applier: (SchemaWrapper<*>, Any?) -> Any?)
    : EditorAction(text, selector) {

    override fun apply(control: TypeControl) {
        val newVal = applier(control.schema, control.getBoundValue())

        control.setBoundValue(newVal)
    }

    override fun shouldBeDisabled(control: TypeControl): Boolean {
        return control.schema.readOnly || super.shouldBeDisabled(control)
    }
}