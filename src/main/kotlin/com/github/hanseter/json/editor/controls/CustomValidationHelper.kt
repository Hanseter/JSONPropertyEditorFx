package com.github.hanseter.json.editor.controls

import javafx.beans.property.Property
import javafx.scene.Node
import org.controlsfx.control.decoration.Decorator
import org.controlsfx.validation.ValidationMessage
import org.controlsfx.validation.ValidationResult
import org.controlsfx.validation.decoration.GraphicValidationDecoration

fun redecorate(node: Node?, vararg messages: Property<ValidationMessage?>) {
    Decorator.removeAllDecorations(node)
    messages.find { it.value != null }?.let {
        GraphicValidationDecoration().applyValidationDecoration(it.value)
    }
}

fun redecorate(node: Node?, results: List<ValidationResult?>) {
    Decorator.removeAllDecorations(node)

    for (result in results.filterNotNull()) {
        if (result.errors.isNotEmpty()) {
            GraphicValidationDecoration().applyValidationDecoration(result.errors.first())
            return
        }
    }

    for (result in results.filterNotNull()) {
        if (result.warnings.isNotEmpty()) {
            GraphicValidationDecoration().applyValidationDecoration(result.warnings.first())
            return
        }
    }
}