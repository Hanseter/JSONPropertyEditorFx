package com.github.hanseter.json.editor.validators

import com.github.hanseter.json.editor.ResourceProvider
import com.github.hanseter.json.editor.actions.TargetSelector
import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import com.github.hanseter.json.editor.types.SupportedType
import com.github.hanseter.json.editor.types.TypeModel
import org.controlsfx.validation.Severity
import org.everit.json.schema.StringSchema
import java.util.function.Supplier

class ResourceValidator(private val resourceProvider: Supplier<ResourceProvider>) : Validator {
    override val selector: TargetSelector =
        TargetSelector { it.supportedType == SupportedType.SimpleType.ResourceType }

    override fun validate(model: TypeModel<*, *>, objId: String): List<Validator.ValidationResult> =
        if (model.value == null || resourceProvider.get().isValidResource(
                model.value as? String,
                objId,
                model.schema as EffectiveSchema<StringSchema>
            )
        ) emptyList()
        else listOf(
            Validator.SimpleValidationResult(
                Severity.ERROR,
                JsonPropertiesMl.bundle.getString("jsonEditor.validators.validResource")
            )
        )

}
