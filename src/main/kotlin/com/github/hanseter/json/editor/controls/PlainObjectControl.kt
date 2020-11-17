package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.types.PlainObjectModel
import com.github.hanseter.json.editor.util.BindableJsonObject
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import com.github.hanseter.json.editor.validators.isRequiredSchema
import org.json.JSONObject

class PlainObjectControl(override val model: PlainObjectModel, context: EditorContext)
    : ObjectControl {

    override val control = TypeWithChildrenStatusControl("Create") {
        model.value = JSONObject()
    }

    private val requiredChildrenNotNull: List<TypeControl>
    private val optionalChildrenNotNull: List<TypeControl>

    override var requiredChildren: List<TypeControl> = emptyList()
    override var optionalChildren: List<TypeControl> = emptyList()

    init {
        val childSchemas = model.schema.baseSchema.propertySchemas.toMutableMap()
        requiredChildrenNotNull = createTypeControlsFromSchemas(model.schema, model.schema.baseSchema.requiredProperties.mapNotNull {
            childSchemas.remove(it)
        }, context)
        optionalChildrenNotNull = createTypeControlsFromSchemas(model.schema, childSchemas.values, context)
        requiredChildren = requiredChildrenNotNull
        optionalChildren = optionalChildrenNotNull
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
                requiredChildren = requiredChildrenNotNull
                optionalChildren = optionalChildrenNotNull
            }

            bindChildrenToObject(subType)
        } else {
            requiredChildren = emptyList()
            optionalChildren = emptyList()
        }
        valueChanged()
    }

    private fun valueChanged() {
        if (model.rawValue == JSONObject.NULL || model.rawValue == null) {
            control.displayNull()
        } else {
            val size = requiredChildren.size + optionalChildren.size
            control.displayNonNull("[${size} Propert${if (size == 1) "y" else "ies"}]")
        }
    }

    private fun createSubType(parent: BindableJsonType): BindableJsonObject? {
        val rawObj = parent.getValue(model.schema)
        if (rawObj == JSONObject.NULL || (rawObj == null && !isRequiredSchema(model.schema))) {
            return null
        }
        var obj = rawObj as? JSONObject
        if (obj == null) {
            obj = JSONObject()
            parent.setValue(model.schema, obj)
        }
        return BindableJsonObject(parent, obj)
    }
}