package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import com.github.hanseter.json.editor.util.BindableJsonType

class UnsupportedTypeModel(override val schema: EffectiveSchema<*>) :
    TypeModel<Any?, SupportedType.SimpleType.UnsupportedType> {
    override val supportedType: SupportedType.SimpleType.UnsupportedType
        get() = SupportedType.SimpleType.UnsupportedType
    override var bound: BindableJsonType? = null
    override val defaultValue: Any?
        get() = schema.defaultValue

    override val previewString: PreviewString
        get() = PreviewString.createPseudo(JsonPropertiesMl.bundle.getString("jsonEditor.controls.notSupported.preview"))

}