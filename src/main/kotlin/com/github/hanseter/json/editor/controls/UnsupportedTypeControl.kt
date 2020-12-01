package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.types.SupportedType
import com.github.hanseter.json.editor.types.TypeModel
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.LazyControl
import javafx.scene.Node
import javafx.scene.control.Label

class UnsupportedTypeControl(override val model: TypeModel<Any?, SupportedType.SimpleType.UnsupportedType>) : TypeControl {
    override val childControls: List<TypeControl>
        get() = emptyList()

    override fun bindTo(type: BindableJsonType) {}
    override fun createLazyControl(): LazyControl = LazyNotSupportedControl()

    private inner class LazyNotSupportedControl : LazyControl {
        override val control: Node
            get() = Label("!!Error: Schema ${model.schema.baseSchema.schemaLocation} with type ${model.schema.baseSchema::class.java.name} cannot be displayed.")

        override fun updateDisplayedValue() {
        }
    }
}