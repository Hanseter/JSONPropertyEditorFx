package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.types.PlainObjectModel
import com.github.hanseter.json.editor.util.BindableJsonObject
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import javafx.beans.value.ObservableBooleanValue
import org.json.JSONObject

class PlainObjectControl(override val model: PlainObjectModel, context: EditorContext)
    : ObjectControl {

    private val editorActionsContainer = context.createActionContainer(this)

    private val statusControl = TypeWithChildrenStatusControl("Create") {
        model.value = JSONObject()
    }

    override val node = FilterableTreeItem(TreeItemData(model.schema.title, model.schema.schema.description, statusControl, editorActionsContainer))

    override val requiredChildren: List<TypeControl>
    override val optionalChildren: List<TypeControl>
    override val valid: ObservableBooleanValue

    init {
//        val validation = ValidationSupport()
        val childSchemas = model.schema.schema.propertySchemas.toMutableMap()
        requiredChildren = createTypeControlsFromSchemas(model.schema, model.schema.schema.requiredProperties.mapNotNull {
            childSchemas.remove(it)
        }, context)
//        requiredChildren.forEach {
//            addNonNullValidation(validation, )
//        }
        optionalChildren = createTypeControlsFromSchemas(model.schema, childSchemas.values, context)
        valid = createValidityBinding(requiredChildren + optionalChildren)

        addRequiredAndOptionalChildren(node, requiredChildren, optionalChildren)
    }


    private fun bindChildrenToObject(json: BindableJsonType) {
        requiredChildren.forEach { it.bindTo(json) }
        optionalChildren.forEach { it.bindTo(json) }
    }

    override fun bindTo(type: BindableJsonType) {
        model.bound = type
        val subType = createSubType(type)

        if (subType != null) {
            if (node.isLeaf) {
                addRequiredAndOptionalChildren(node, requiredChildren, optionalChildren)
            }

            bindChildrenToObject(subType)
        } else {
            node.clear()
        }
        valueChanged()
    }

    private fun valueChanged() {
        if (model.rawValue == JSONObject.NULL) {
            statusControl.displayNull()
        } else {
            val size = requiredChildren.size + optionalChildren.size
            statusControl.displayNonNull("[${size} Propert${if (size == 1) "y" else "ies"}]")
        }
    }

    private fun createSubType(parent: BindableJsonType): BindableJsonObject? {
        val rawObj = parent.getValue(model.schema)
        if (rawObj == JSONObject.NULL) {
            return null
        }
        var obj = rawObj as? JSONObject
        if (obj == null) {
            obj = JSONObject()
            parent.setValue(model.schema, obj)
        }
        return BindableJsonObject(parent, obj)
    }

//    fun addNonNullValidation(validationSupport: ValidationSupport, child: TypeControl) {
//        val validationResult = child.node.value.control
//        valida
//        val validator = createNonNullValidator(va)
//    }
//
//    fun createNonNullValidator(child: TypeControl): org.controlsfx.validation.Validator<Any?> {
//        Validator { control, value ->
//
//            ValidationResult.fromErrorIf(
//                    control,
//                    validationResult,
//                    validationResult != null
//            )
//        }
//    }
}