package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.types.CombinedObjectModel
import com.github.hanseter.json.editor.util.BindableJsonType
import javafx.scene.Node

class CombinedObjectControl(override val model: CombinedObjectModel, val controls: List<ObjectControl>)
    : ObjectControl {

    // Note: This control does not properly support explicit null values.
    // However, it breaks if it appears anywhere but the root anyway, so that's not a real problem (yet)
    override val requiredChildren: List<TypeControl> = controls.flatMap { it.requiredChildren }.distinctBy { it.model.schema.title }
    override val optionalChildren: List<TypeControl> = controls.flatMap { it.optionalChildren }.distinctBy { it.model.schema.title }

    override val control: Node?
        get() = null

    override fun bindTo(type: BindableJsonType) {
        controls.forEach { it.bindTo(type) }
        model.bound = type
    }

}