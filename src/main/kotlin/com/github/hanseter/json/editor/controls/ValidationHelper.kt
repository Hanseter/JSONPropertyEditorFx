package com.github.hanseter.json.editor.controls

import javafx.beans.property.Property
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.controlsfx.control.decoration.Decorator
import org.controlsfx.validation.ValidationMessage
import org.controlsfx.validation.ValidationResult
import org.controlsfx.validation.Validator
import org.controlsfx.validation.decoration.GraphicValidationDecoration

fun redecorate(node: Node?, vararg messages: Property<ValidationMessage?>) {
    Decorator.removeAllDecorations(node)
    messages.find { it.value != null }?.let {
        GraphicValidationDecoration().applyValidationDecoration(it.value)
    }
}

fun redecorate(node: Node?, results: List<ValidationResult?>) {
    Decorator.removeAllDecorations(node)

    results.filterNotNull().firstOrNull() { it.errors.isNotEmpty() }?.let {
        GraphicValidationDecoration().applyValidationDecoration(it.errors.first())
        return
    }

    results.filterNotNull().firstOrNull() { it.warnings.isNotEmpty() }?.let {
        GraphicValidationDecoration().applyValidationDecoration(it.warnings.first())
        return
    }
}

fun runAfterSkinLoads(node: Control, runnable: () -> Unit) {
    if (node.skin != null) {
        runnable()
    } else {
        node.skinProperty().addListener(object : ChangeListener<Skin<*>> {
            override fun changed(observable: ObservableValue<out Skin<*>>?, oldValue: Skin<*>?, newValue: Skin<*>?) {
                if (newValue != null) {
                    runnable()
                    node.skinProperty().removeListener(this)
                }
            }

        })
    }
}

fun <T> combineValidators(vararg validators: Validator<T>?): Validator<T> {
    return Validator.combine(*validators.filterNotNull().toTypedArray())
}