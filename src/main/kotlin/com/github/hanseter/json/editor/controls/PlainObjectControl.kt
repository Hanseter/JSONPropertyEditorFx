package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.types.PlainObjectModel
import com.github.hanseter.json.editor.util.BindableJsonObject
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import com.github.hanseter.json.editor.util.LazyControl
import org.json.JSONObject

class PlainObjectControl(override val model: PlainObjectModel, context: EditorContext)
    : ObjectControl {

    override val allRequiredChildren: List<TypeControl>
    override val allOptionalChildren: List<TypeControl>

    override var requiredChildren: List<TypeControl> = emptyList()
    override var optionalChildren: List<TypeControl> = emptyList()

    init {
        val childSchemas = model.schema.baseSchema.propertySchemas.toMutableMap()
        allRequiredChildren = createTypeControlsFromSchemas(model.schema, model.schema.baseSchema.requiredProperties.mapNotNull {
            childSchemas.remove(it)
        }, context)
        allOptionalChildren = createTypeControlsFromSchemas(model.schema, childSchemas.values, context)
        requiredChildren = allRequiredChildren
        optionalChildren = allOptionalChildren
    }


    private fun bindChildrenToObject(json: BindableJsonType) {
        requiredChildren.forEach { it.bindTo(json) }
        optionalChildren.forEach { it.bindTo(json) }
    }

    override fun bindTo(type: BindableJsonType) {
        model.bound = type
        val subType = createSubType(type)

        if (subType != null) {
            if (requiredChildren.isEmpty() && optionalChildren.isEmpty()) {
                requiredChildren = allRequiredChildren
                optionalChildren = allOptionalChildren
            }

            bindChildrenToObject(subType)
        } else {
            requiredChildren = emptyList()
            optionalChildren = emptyList()
        }
    }

    override fun createLazyControl(): LazyControl = ObjectControl.LazyObjectControl(this)

    private fun createSubType(parent: BindableJsonType): BindableJsonObject? {
        val rawObj = parent.getValue(model.schema)
        if (rawObj == JSONObject.NULL) return null
        var obj = rawObj as? JSONObject ?: model.defaultValue
        if (obj == null) {
            if (!model.schema.required) return null
            obj = JSONObject()
            parent.setValue(model.schema, obj)
        }
        return BindableJsonObject(parent, obj)
    }
}