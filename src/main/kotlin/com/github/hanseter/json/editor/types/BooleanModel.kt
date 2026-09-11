package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import org.everit.json.schema.BooleanSchema

class BooleanModel(
    override val schema: EffectiveSchema<BooleanSchema>,
    private val context: EditorContext
) :
    TypeModel<Boolean?, SupportedType.SimpleType.BooleanType> {
    override val supportedType: SupportedType.SimpleType.BooleanType
        get() = SupportedType.SimpleType.BooleanType
    override var bound: BindableJsonType? = null
    override val defaultValue: Boolean?
        get() = context.customizationObject().getNullBooleanPreview(this)

    override val previewString: PreviewString
        get() = PreviewString.create(boolToString(value), boolToString(defaultValue), rawValue)

    companion object {
        private fun boolToString(boolean: Boolean?): String? {
            if (boolean == null) return null

            return if (boolean) {
                JsonPropertiesMl.bundle.getString("jsonEditor.model.boolean.true")
            } else {
                JsonPropertiesMl.bundle.getString("jsonEditor.model.boolean.false")
            }
        }

    }
}