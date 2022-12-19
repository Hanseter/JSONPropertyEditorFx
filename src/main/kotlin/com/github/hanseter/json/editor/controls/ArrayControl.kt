package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.ControlFactory
import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.extensions.EffectiveSchemaInArray
import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import com.github.hanseter.json.editor.types.ArrayModel
import com.github.hanseter.json.editor.types.PreviewString
import com.github.hanseter.json.editor.types.TypeModel
import com.github.hanseter.json.editor.util.*
import org.everit.json.schema.ArraySchema
import org.json.JSONArray
import org.json.JSONObject


class ArrayControl(override val model: ArrayModel, private val context: EditorContext) :
    TypeControl {
    override val childControls = mutableListOf<TypeControl>()
    private var subArray: BindableJsonArray? = null

    override fun bindTo(type: BindableJsonType) {
        model.bound = type
        subArray = createSubArray(type, model.schema, model)
        valuesChanged()
    }

    override fun createLazyControl(): LazyControl = LazyArrayControl()

    private inner class LazyArrayControl : LazyControl {
        override val control = TypeWithChildrenStatusControl(JsonPropertiesMl.bundle.getString("jsonEditor.control.array.create")) {
            model.value = JSONArray()
            valuesChanged()
        }.apply {
            isDisable = model.schema.readOnly
        }

        override fun updateDisplayedValue() {
            updateLabel()
        }

        private fun updateLabel() {
            if (model.rawValue == JSONObject.NULL || (model.rawValue == null && model.defaultValue == null)) {
                control.displayNull()
            } else {
                control.displayNonNull(model.previewString.string)
            }
        }
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
                val childSchema =
                    EffectiveSchemaInArray(model.schema, model.contentSchema, currentChildIndex)

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
    }
}

fun createSubArray(
    parent: BindableJsonType,
    schema: EffectiveSchema<ArraySchema>,
    model: TypeModel<JSONArray?, *>
): BindableJsonArray? {
    val rawArr = parent.getValue(schema)

    if (rawArr == JSONObject.NULL) return null
    var arr = rawArr as? JSONArray ?: model.defaultValue
    if (arr == null) {
        if (!schema.required) return null
        arr = JSONArray()
        parent.setValue(schema, arr)
    }
    return BindableJsonArray(parent, arr)
}