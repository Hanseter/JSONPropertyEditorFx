package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.actions.ActionsContainer
import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.TextField
import org.controlsfx.validation.Severity
import org.controlsfx.validation.ValidationResult
import org.controlsfx.validation.ValidationSupport
import org.controlsfx.validation.Validator
import org.everit.json.schema.FormatValidator
import org.everit.json.schema.StringSchema
import java.util.regex.Pattern

class StringControl(override val schema: SchemaWrapper<StringSchema>, context: EditorContext) : TypeControl, ControlProvider<String> {
    override val control = TextField()
    override val value: Property<String?>
        get() = control.textProperty()
    override val defaultValue: String?
        get() = schema.schema.defaultValue as? String
    override val editorActionsContainer: ActionsContainer = context.createActionContainer(this)
    private val delegate = RowBasedControl(this)
    override val node: FilterableTreeItem<TreeItemData> = delegate.node
    override val valid = SimpleBooleanProperty(true)

    init {
        val validation = ValidationSupport()
        validation.initInitialDecoration()

        validation.registerValidator(control, false, combineValidators(
                createFormatValidation(schema.schema.formatValidator),
                createLengthValidation(schema.schema.minLength, schema.schema.maxLength),
                createPatternValidation(schema.schema.pattern)
        ))

        valid.bind(validation.invalidProperty().not())
    }

    override fun bindTo(type: BindableJsonType) {
        delegate.bindTo(type, STRING_CONVERTER)

        control.promptText = if (delegate.isBoundToNull()) TypeControl.NULL_PROMPT else ""
    }

    companion object {
        val STRING_CONVERTER: (Any) -> String = { it as? String ?: it.toString() }

        fun addFormatValidation(
                validation: ValidationSupport,
                textField: TextField,
                formatValidator: FormatValidator?
        ): Validator<String?>? {
            val validator = createFormatValidation(formatValidator) ?: return null
            validation.registerValidator(textField, false, validator)
            return validator
        }

        fun createFormatValidation(format: FormatValidator?): Validator<String?>? = if (format == null) {
            null
        } else {
            Validator { control, value ->
                val validationResult = value?.let { format.validate(value).orElse(null) }
                ValidationResult.fromErrorIf(
                        control,
                        validationResult,
                        validationResult != null
                )
            }
        }

        fun addLengthValidation(
                validation: ValidationSupport,
                textField: TextField,
                minLength: Int?,
                maxLength: Int?
        ): Validator<String?>? {
            val validator = createLengthValidation(minLength, maxLength) ?: return null
            validation.registerValidator(textField, false, validator)
            return validator
        }

        fun createLengthValidation(minLength: Int?, maxLength: Int?): Validator<String?>? = when {
            minLength != null && maxLength != null -> Validator { control, value ->
                ValidationResult.fromErrorIf(
                        control,
                        "Has to be $minLength to $maxLength characters",
                        value?.length?.let { it < minLength || it > maxLength } ?: false
                )
            }
            minLength != null -> Validator { control, value ->
                ValidationResult.fromErrorIf(
                        control,
                        "Has to be at least $minLength characters",
                        value?.length?.let { it < minLength } ?: false
                )
            }
            maxLength != null -> Validator { control, value ->
                ValidationResult.fromErrorIf(
                        control,
                        "Has to be at most $maxLength characters",
                        value?.length?.let { it > maxLength } ?: false
                )
            }
            else -> null
        }

        fun addPatternValidation(
                validation: ValidationSupport,
                textField: TextField,
                pattern: Pattern?
        ): Validator<String?>? {
            if (pattern == null) return null
            // not using createRegexValidator because that returns an error on null, we want Ok on null
            val validator = Validator.createPredicateValidator({ it: String? ->
                it == null || pattern.matcher(it).matches()
            }, "Has to match pattern $pattern", Severity.ERROR)
            validation.registerValidator(textField, false, validator)
            return validator
        }

        fun createPatternValidation(pattern: Pattern?): Validator<String?>? = when (pattern) {
            null -> null
            else -> Validator.createPredicateValidator({ it: String? ->
                it == null || pattern.matcher(it).matches()
            }, "Has to match pattern $pattern", Severity.ERROR)
        }
    }
}