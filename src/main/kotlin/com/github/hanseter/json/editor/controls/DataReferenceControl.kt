package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.actions.ActionsContainer
import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.util.*
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ChangeListener
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.controlsfx.control.textfield.TextFields
import org.controlsfx.validation.Severity
import org.controlsfx.validation.ValidationResult
import org.controlsfx.validation.Validator
import org.everit.json.schema.StringSchema
import org.json.JSONObject
import java.util.regex.Pattern

class DataReferenceControl(override val schema: SchemaWrapper<StringSchema>, private val context: EditorContext) : TypeControl {

    private val value: Property<String?> = SimpleStringProperty(null)

    private val idField = LabelledTextField().apply {
        styleClass.addAll("data-reference-input", "data-reference-id-input")
        HBox.setHgrow(this, Priority.ALWAYS)
    }
    private val dataField = TextField().apply {
        styleClass.addAll("data-reference-input", "data-reference-data-input")
        HBox.setHgrow(this, Priority.ALWAYS)
    }
    private val label = Label("#").apply {
        styleClass += "data-reference-label"
    }
    private val control = HBox(
            idField,
            label,
            dataField
    ).apply {
        styleClass += "data-reference-container"
    }

    private val editorActionsContainer: ActionsContainer = context.createActionContainer(this)
    override val node = FilterableTreeItem(TreeItemData(schema.title, null, control, editorActionsContainer))

    private var bound: BindableJsonType? = null
    override val valid = SimpleBooleanProperty(true)

    private val formatValidator: Validator<String>?
    private val lengthValidator: Validator<String>?
    private val fullPatternValidator: Validator<String>?
    private val idPatternValidator: Validator<String>?
    private val dataPatternValidator: Validator<String>?

    private val validIdValidator: Validator<String>?
    private val validDataPointerValidator: Validator<String>?

    init {
        formatValidator = StringControl.createFormatValidation(schema.schema.formatValidator)
        lengthValidator = StringControl.createLengthValidation(schema.schema.minLength, schema.schema.maxLength)
        fullPatternValidator = schema.schema.pattern?.let {
            Validator.createRegexValidator("Has to match pattern $it", it, Severity.ERROR)
        }

        val idPattern = (schema.schema.unprocessedProperties["idPattern"] as? String)?.let { Pattern.compile(it) }

        idPatternValidator = idPattern?.let { Validator.createRegexValidator("Has to match pattern $it", it, Severity.ERROR) }

        TextFields.bindAutoCompletion(idField) { request ->
            val proposals = context.refProvider.calcCompletionProposals(request.userText)
            if (idPattern != null) {
                proposals.filter { idPattern.matcher(it).matches() }
            } else {
                proposals
            }
        }

        validIdValidator = Validator { control, currentValue ->
            val invalid = !context.refProvider.isValidReference(currentValue)
            ValidationResult.fromErrorIf(control, "Has to be a valid reference", invalid)
        }

        val dataPattern = (schema.schema.unprocessedProperties["dataPattern"] as? String)?.let { Pattern.compile(it) }

        dataPatternValidator = dataPattern?.let { Validator.createRegexValidator("Has to match pattern $it", it, Severity.ERROR) }

        TextFields.bindAutoCompletion(dataField) { request ->
            val boundId = getBoundId()
            if (boundId != null && context.refProvider.isValidReference(boundId)) {
                val data = context.refProvider.getDataAndSchema(boundId)?.data

                val allPointers = data?.let { getAllJsonPointerStrings(it, request.userText) }
                        ?: listOf()

                if (dataPattern != null) {
                    allPointers.filter { dataPattern.matcher(it).matches() }
                } else {
                    allPointers
                }
            } else {
                listOf<String>()
            }
        }

        validDataPointerValidator = Validator { control, currentValue ->
            val valid = getBoundId()?.let { context.refProvider.getDataAndSchema(it)?.data }?.let {
                isValidJsonPointer(it, currentValue)
            } ?: true

            ValidationResult.fromErrorIf(control, "Has to be a pointer to valid data", !valid)
        }

        idField.textProperty().addListener { _, _, _ -> updateValueFromUi() }
        dataField.textProperty().addListener { _, _, _ -> updateValueFromUi() }

        value.addListener(ChangeListener { _, _, new -> bound?.setValue(schema, new) })
    }

    override fun bindTo(type: BindableJsonType) {
        bound = null
        val rawVal = type.getValue(schema)

        val toAssign = when (rawVal) {
            JSONObject.NULL -> null
            null -> schema.schema.defaultValue as? String
            else -> rawVal as? String
        }

        val changed = toAssign != value.value

        if (changed) {
            value.value = toAssign
            updateUi()
        }
        bound = type
        editorActionsContainer.updateDisablement()
        valueChanged()
    }

    private fun valueChanged() {
        val promptText = if (JSONObject.NULL == bound?.getValue(schema)) TypeControl.NULL_PROMPT else ""
        idField.promptText = promptText
        dataField.promptText = promptText

        runValidation()

        val newDesc = context.refProvider.getReferenceDescription(getBoundId()) ?: ""
        idField.label = if (newDesc.isNotEmpty()) " ($newDesc)" else ""
        
    }

    private fun runValidation() {
        val fullValue = getFullValue()
        val id = getBoundId()
        val dataPointer = getBoundDataPointer()

        redecorate(label, listOf(
                fullPatternValidator?.apply(label, fullValue),
                lengthValidator?.apply(label, fullValue),
                formatValidator?.apply(label, fullValue)
        ))

        redecorate(idField, listOf(
                idPatternValidator?.apply(idField, id),
                validIdValidator?.apply(idField, id)
        ))

        redecorate(dataField, listOf(
                dataPatternValidator?.apply(dataField, dataPointer),
                validDataPointerValidator?.apply(dataField, dataPointer)
        ))
    }

    private fun getFullValue(): String? {
        return bound?.getValue(schema) as? String
    }

    private fun getBoundId(): String? {
        val fullValue = getFullValue()

        val sepIndex = fullValue?.indexOf('#') ?: -1

        return if (sepIndex >= 0) fullValue?.substring(0, sepIndex) else ""
    }

    private fun getBoundDataPointer(): String? {
        val fullValue = getFullValue()

        val sepIndex = fullValue?.indexOf('#') ?: -1

        return if (sepIndex >= 0) fullValue?.substring(sepIndex + 1) else ""
    }

    private fun updateUi() {
        val sepIndex = (value.value ?: "").indexOf('#')

        if (value.value == "#" || sepIndex < 0) {
            idField.text = ""
            dataField.text = ""
        } else {
            val newId = value.value!!.substring(0, sepIndex)
            val newData = value.value!!.substring(sepIndex + 1)
            if (newId != idField.text) {
                idField.text = newId
            }
        }
    }


    private fun updateValueFromUi() {
        if (idField.text.isNullOrEmpty() && dataField.text.isNullOrEmpty()) {
            value.value = ""
        } else {
            value.value = "${idField.text}#${dataField.text}"
        }
    }
}
