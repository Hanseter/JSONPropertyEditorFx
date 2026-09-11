package com.github.hanseter.json.editor.util

import com.github.hanseter.json.editor.types.IdReferenceModel
import com.github.hanseter.json.editor.types.IntegerModel
import com.github.hanseter.json.editor.types.SupportedType
import com.github.hanseter.json.editor.types.TypeModel

/**
 * An object that can be used to customize the behavior and appearance of the editor.
 */
interface CustomizationObject {

    /**
     * Gets the title for a given element.
     */
    fun getTitle(model: TypeModel<*, *>, defaultTitle: String): String {
        return defaultTitle
    }

    /**
     * Gets the description for a given element.
     */
    fun getDescription(model: TypeModel<*, *>, defaultDescription: String?): String? {
        return defaultDescription
    }

    fun getNullStringPreview(model: TypeModel<String?, SupportedType.SimpleType.StringType>): String? =
        model.schema.defaultValue as? String

    fun getNullIdReferencePreview(model: TypeModel<String?, SupportedType.SimpleType.IdReferenceType>): String? =
        model.schema.defaultValue as? String

    fun getNullIntPreview(model: TypeModel<Int?, SupportedType.SimpleType.IntType>): Int? =
        (model.schema.defaultValue as? Number)?.toInt()

    fun getNullDoublePreview(model: TypeModel<Double?, SupportedType.SimpleType.DoubleType>): Double? =
        (model.schema.defaultValue as? Number)?.toDouble()

    fun getNullBooleanPreview(model: TypeModel<Boolean?, SupportedType.SimpleType.BooleanType>): Boolean? =
        model.schema.defaultValue as? Boolean

    fun getNullEnumPreview(model: TypeModel<String?, SupportedType.SimpleType.EnumType>): String? =
        model.schema.defaultValue as? String

    fun getNullColorPreview(model: TypeModel<String?, SupportedType.SimpleType.ColorType>): String? =
        model.schema.defaultValue as? String


}

object DefaultCustomizationObject : CustomizationObject