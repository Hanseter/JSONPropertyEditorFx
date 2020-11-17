package com.github.hanseter.json.editor.types

import javafx.beans.property.Property
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import org.json.JSONObject

class ModelControlSynchronizer<T>(private val controlProp: Property<T>, private val model: TypeModel<T?, *>) : ChangeListener<T?> {
    init {
        modelChanged()
    }

    override fun changed(observable: ObservableValue<out T?>?, oldValue: T?, newValue: T?) {
        model.value = newValue
    }

    fun modelChanged() {
        controlProp.removeListener(this)
        val newVal = model.value
                ?: (if (model.rawValue == JSONObject.NULL) null else model.defaultValue)
        if (controlProp.value != newVal) {
            controlProp.value = newVal
        }
        controlProp.addListener(this)
    }
}