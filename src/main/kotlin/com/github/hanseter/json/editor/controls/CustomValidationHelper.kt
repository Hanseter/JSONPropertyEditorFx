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

    results.filterNotNull().firstOrNull() { it.errors.isNotEmpty() }?.let {
        GraphicValidationDecoration().applyValidationDecoration(it.errors.first())
        return
    }

    results.filterNotNull().firstOrNull() { it.warnings.isNotEmpty() }?.let {
        GraphicValidationDecoration().applyValidationDecoration(it.warnings.first())
        return
    }
}