package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.controls.TypeControl
import com.github.hanseter.json.editor.extensions.SchemaWrapper

/**
 *
 */
abstract class EditorAction(val text: String, private val selector: ActionTargetSelector) {

    fun matches(schema: SchemaWrapper<*>) = selector.matches(schema)

    abstract fun apply(control: TypeControl)

    var disablePredicate: (schema: SchemaWrapper<*>, value: Any?) -> Boolean = { _, _ -> false }

    open fun shouldBeDisabled(control: TypeControl): Boolean {
        return disablePredicate(control.schema, control.getBoundValue())
    }

    var description: String = ""
}