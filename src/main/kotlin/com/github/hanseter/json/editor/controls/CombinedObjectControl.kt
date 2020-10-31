package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.types.CombinedObjectModel
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import javafx.beans.value.ObservableBooleanValue

class CombinedObjectControl(override val model: CombinedObjectModel, val controls: List<ObjectControl>, context: EditorContext)
    : ObjectControl {

    // Note: This control does not properly support explicit null values.
    // However, it breaks if it appears anywhere but the root anyway, so that's not a real problem (yet)

    override val valid: ObservableBooleanValue = createValidityBinding(controls)
    override val requiredChildren: List<TypeControl> = controls.flatMap { it.requiredChildren }.distinctBy { it.model.schema.title }
    override val optionalChildren: List<TypeControl> = controls.flatMap { it.optionalChildren }.distinctBy { it.model.schema.title }

    private val editorActionsContainer = context.createActionContainer(this)

    override val node = FilterableTreeItem(TreeItemData(model.schema.title, model.schema.schema.description, null, editorActionsContainer))

    init {
        addRequiredAndOptionalChildren(node, requiredChildren, optionalChildren)
    }

    override fun bindTo(type: BindableJsonType) {
        controls.forEach { it.bindTo(type) }
        model.bound = type
    }

}