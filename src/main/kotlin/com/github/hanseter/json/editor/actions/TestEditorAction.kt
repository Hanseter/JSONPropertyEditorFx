package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.controls.TypeControl

class TestEditorAction(text: String, selector: ActionTargetSelector)
    : EditorAction(text, selector) {


    override fun apply(control: TypeControl) {}

}