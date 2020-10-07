package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.util.BindableJsonObject
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import javafx.beans.value.ObservableBooleanValue
import org.everit.json.schema.ObjectSchema
import org.json.JSONObject

class PlainObjectControl(override val schema: SchemaWrapper<ObjectSchema>, context: EditorContext)
    : ObjectControl {

    private val editorActionsContainer = context.createActionContainer(this)

    override val node = FilterableTreeItem(TreeItemData(schema.title, null, null, editorActionsContainer))

    private var bound: BindableJsonType? = null

    override val requiredChildren: List<TypeControl>
    override val optionalChildren: List<TypeControl>
    override val valid: ObservableBooleanValue

    init {
        val childSchemas = schema.schema.propertySchemas.toMutableMap()
        requiredChildren = createTypeControlsFromSchemas(schema, schema.schema.requiredProperties.mapNotNull {
            childSchemas.remove(it)
        }, context)
        optionalChildren = createTypeControlsFromSchemas(schema, childSchemas.values, context)
        valid = createValidityBinding(requiredChildren + optionalChildren)

        addRequiredAndOptionalChildren(node, requiredChildren, optionalChildren)
    }


    private fun bindChildrenToObject(json: BindableJsonType) {
        requiredChildren.forEach { it.bindTo(json) }
        optionalChildren.forEach { it.bindTo(json) }
    }

    override fun bindTo(type: BindableJsonType) {
        bindChildrenToObject(createSubType(type))
        bound = type
    }

    private fun createSubType(parent: BindableJsonType): BindableJsonObject {
        var obj = parent.getValue(schema) as? JSONObject
        if (obj == null) {
            obj = JSONObject()
            parent.setValue(schema, obj)
        }
        return BindableJsonObject(parent, obj)
    }
}