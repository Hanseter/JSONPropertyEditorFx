package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonType
import javafx.beans.value.ObservableBooleanValue
import org.everit.json.schema.CombinedSchema

class CombinedObjectControl(schema: SchemaWrapper<CombinedSchema>, val controls: List<ObjectControl>)
    : TypeWithChildrenControl(schema), ObjectControl {
    override val schema: SchemaWrapper<*> = schema
    override val valid: ObservableBooleanValue = createValidityBinding(controls)
    override val requiredChildren: List<TypeControl> = controls.flatMap { it.requiredChildren }.distinctBy { it.schema.title }
    override val optionalChildren: List<TypeControl> = controls.flatMap { it.optionalChildren }.distinctBy { it.schema.title }

    init {
        addRequiredAndOptionalChildren(node, requiredChildren, optionalChildren)
    }

    override fun bindTo(type: BindableJsonType) {
        controls.forEach { it.bindTo(type) }
    }
}