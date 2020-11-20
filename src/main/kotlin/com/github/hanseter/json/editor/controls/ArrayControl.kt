package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.ControlFactory
import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.extensions.EffectiveSchemaInArray
import com.github.hanseter.json.editor.types.ArrayModel
import com.github.hanseter.json.editor.util.BindableJsonArray
import com.github.hanseter.json.editor.util.BindableJsonArrayEntry
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import com.github.hanseter.json.editor.validators.isRequiredSchema
import org.everit.json.schema.ArraySchema
import org.json.JSONArray
import org.json.JSONObject


class ArrayControl(override val model: ArrayModel, private val context: EditorContext) : TypeControl {
    override val control = TypeWithChildrenStatusControl("To Empty List") {
        model.value = JSONArray()
        valuesChanged()
    }
    override val childControls = mutableListOf<TypeControl>()
    private var subArray: BindableJsonArray? = null

    override fun bindTo(type: BindableJsonType) {
        model.bound = type
        subArray = createSubArray(type, model.schema)
        valuesChanged()
    }

    private fun valuesChanged() {
        fun rebindChildren(subArray: BindableJsonArray?, values: JSONArray) {
            if (subArray != null) {
                for (i in 0 until values.length()) {
                    val obj = BindableJsonArrayEntry(subArray, i)
                    childControls[i].bindTo(obj)
                }
            }
        }

        fun addChildControls(values: JSONArray) {
            while (childControls.size < values.length()) {
                val currentChildIndex = childControls.size
                val childSchema = EffectiveSchemaInArray(model.schema, model.contentSchema, currentChildIndex)

                childControls.add(ControlFactory.convert(childSchema, context))
            }
        }

        fun removeChildControls(values: JSONArray) {
            while (childControls.size > values.length()) {
                childControls.removeAt(childControls.size - 1)
            }
        }

        val subArray = subArray
        val rawValues = model.rawValue
        val values = rawValues as? JSONArray
        if (rawValues != JSONObject.NULL && values != null) {
            removeChildControls(values)
            addChildControls(values)
            rebindChildren(subArray, values)
            model.value = values
        } else {
            childControls.clear()
        }

        updateLabel()
    }

    private fun updateLabel() {
        if (model.rawValue == JSONObject.NULL || model.rawValue == null) {
            control.displayNull()
        } else {
            control.displayNonNull("[${childControls.size} Element${if (childControls.size == 1) "" else "s"}]")
        }
    }
}

fun createSubArray(parent: BindableJsonType, schema: EffectiveSchema<ArraySchema>): BindableJsonArray? {
    val rawArr = parent.getValue(schema)

    if (rawArr == JSONObject.NULL || (rawArr == null && !isRequiredSchema(schema))) {
        return null
    }

    var arr = rawArr as? JSONArray
    if (arr == null) {
        arr = JSONArray()
        parent.setValue(schema, arr)
    }
    return BindableJsonArray(parent, arr)
}