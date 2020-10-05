package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.IdReferenceProposalProvider
import com.github.hanseter.json.editor.ResolutionScopeProvider
import com.github.hanseter.json.editor.actions.EditorAction
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonObject
import com.github.hanseter.json.editor.util.BindableJsonType
import javafx.beans.value.ObservableBooleanValue
import org.everit.json.schema.ObjectSchema
import org.json.JSONObject

class PlainObjectControl(
        override val schema: SchemaWrapper<ObjectSchema>,
        refProvider: IdReferenceProposalProvider,
        resolutionScopeProvider: ResolutionScopeProvider,
        actions: List<EditorAction>
) : TypeWithChildrenControl(schema, actions), ObjectControl {

    override val requiredChildren: List<TypeControl>
    override val optionalChildren: List<TypeControl>
    override val valid: ObservableBooleanValue

    init {
        val childSchemas = schema.schema.propertySchemas.toMutableMap()
        requiredChildren = createTypeControlsFromSchemas(schema.schema.requiredProperties.mapNotNull {
            childSchemas.remove(it)
        }, refProvider, resolutionScopeProvider, actions)
        optionalChildren = createTypeControlsFromSchemas(childSchemas.values, refProvider, resolutionScopeProvider, actions)
        valid = createValidityBinding(requiredChildren + optionalChildren)

        addRequiredAndOptionalChildren(node, requiredChildren, optionalChildren)
    }


    private fun bindChildrenToObject(json: BindableJsonType) {
        requiredChildren.forEach { it.bindTo(json) }
        optionalChildren.forEach { it.bindTo(json) }
    }

    override fun bindTo(type: BindableJsonType) {
        bindChildrenToObject(createSubType(type))
        super.bindTo(type)
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