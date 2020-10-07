package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import javafx.beans.value.ObservableBooleanValue
import org.everit.json.schema.CombinedSchema

class CombinedObjectControl(override val schema: SchemaWrapper<CombinedSchema>, val controls: List<ObjectControl>, context: EditorContext)
    : ObjectControl {
    override val valid: ObservableBooleanValue = createValidityBinding(controls)
    override val requiredChildren: List<TypeControl> = controls.flatMap { it.requiredChildren }.distinctBy { it.schema.title }
    override val optionalChildren: List<TypeControl> = controls.flatMap { it.optionalChildren }.distinctBy { it.schema.title }

    private val editorActionsContainer = context.createActionContainer(this)

    override val node = FilterableTreeItem(TreeItemData(schema.title, null, null, editorActionsContainer))

    private var bound: BindableJsonType? = null

    init {
        addRequiredAndOptionalChildren(node, requiredChildren, optionalChildren)
    }


    override fun bindTo(type: BindableJsonType) {
        controls.forEach { it.bindTo(type) }
        bound = type
    }

}