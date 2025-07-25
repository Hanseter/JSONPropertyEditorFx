package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.controls.TypeControl
import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.util.EditorContext

/**
 * A factory that creates controls for different properties.
 * The properties are preprocessed to a [EffectiveSchema] before being fed to this factory.
 * This factory can then check if it can create a custom control for a specific property, if not it can delegate to the default [ControlFactory].
 */
interface PropertyControlFactory {

    /**
     * Creates a new [TypeControl] for the property described by the [schema].
     */
    fun create(schema: EffectiveSchema<*>, context: EditorContext): TypeControl
}