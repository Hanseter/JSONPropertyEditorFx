package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.types.ModelControlSynchronizer
import com.github.hanseter.json.editor.types.TypeModel
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.LazyControl
import javafx.scene.control.Control
import org.json.JSONObject

class RowBasedControl<T>(
    private val controlWithPropertyProvider: () -> ControlWithProperty<T?>,
    override val model: TypeModel<T?, *>
) : TypeControl {

    override val childControls: List<TypeControl>
        get() = emptyList()

    override fun bindTo(type: BindableJsonType) {
        model.bound = type
    }

    override fun createLazyControl(): LazyControl = SimpleLazyControl(controlWithPropertyProvider())

    private inner class SimpleLazyControl(val controlWithProperty: ControlWithProperty<T?>) :
        LazyControl {
        private val synchronizer = ModelControlSynchronizer(controlWithProperty.property, model)
        override val control: Control
            get() = controlWithProperty.control

        init {
            controlWithProperty.control.isDisable = model.schema.readOnly
        }

        override fun updateDisplayedValue() {
            val rawVal = model.rawValue
            synchronizer.modelChanged()
            updateStyleClasses(rawVal)
            controlWithProperty.previewNull(null == rawVal)
        }

        private fun updateStyleClasses(rawVal: Any?) {
            controlWithProperty.control.styleClass.removeAll(
                NULL_VALUE_CSS_CLASS,
                DEFAULT_VALUE_CSS_CLASS
            )

            if (rawVal == JSONObject.NULL) {
                if (NULL_VALUE_CSS_CLASS !in controlWithProperty.control.styleClass) {
                    controlWithProperty.control.styleClass += NULL_VALUE_CSS_CLASS
                }
            } else if (rawVal == null) {
                if (model.defaultValue != null) {
                    if (DEFAULT_VALUE_CSS_CLASS !in controlWithProperty.control.styleClass) {
                        controlWithProperty.control.styleClass += DEFAULT_VALUE_CSS_CLASS
                    }
                } else {
                    if (NULL_VALUE_CSS_CLASS !in controlWithProperty.control.styleClass) {
                        controlWithProperty.control.styleClass += NULL_VALUE_CSS_CLASS
                    }
                }
            }
        }
    }

    private fun isBoundToNull(rawVal: Any?): Boolean =
        !isBoundToDefault(rawVal) && JSONObject.NULL == rawVal

    private fun isBoundToDefault(rawVal: Any?): Boolean =
        model.defaultValue != null && null == rawVal

    companion object {
        const val NULL_VALUE_CSS_CLASS = "has-null-value"
        const val DEFAULT_VALUE_CSS_CLASS = "has-default-value"
    }
}