package com.github.hanseter.json.editor.validators

import com.github.hanseter.json.editor.actions.TargetSelector
import com.github.hanseter.json.editor.types.TypeModel
import org.controlsfx.validation.Severity

/**
 * A validator can be applied to different parts of the displayed objects. The validation returns a list of errors.
 */
interface Validator {
    /**
     * A selector which determines whether the validator shall be applied to the current data.
     */
    val selector: TargetSelector

    /**
     * Returns an error message if validation fails.
     */
    fun validate(model: TypeModel<*,*>, objId: String): List<ValidationResult>

    interface ValidationResult {

        val message: String

        val severity: Severity

    }

    class SimpleValidationResult(
        override val severity: Severity,
        override val message: String
    ) : ValidationResult


}