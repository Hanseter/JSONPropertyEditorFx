package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.IdReferenceProposalProvider
import com.github.hanseter.json.editor.JsonPropertiesEditor
import com.github.hanseter.json.editor.ResolutionScopeProvider
import com.github.hanseter.json.editor.actions.ActionTargetSelector
import com.github.hanseter.json.editor.actions.EditorAction
import com.github.hanseter.json.editor.actions.ReadOnlyAction
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.util.BindableJsonType
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.controlsfx.control.PopOver
import org.controlsfx.control.textfield.TextFields
import org.controlsfx.validation.ValidationResult
import org.controlsfx.validation.ValidationSupport
import org.controlsfx.validation.Validator
import org.everit.json.schema.StringSchema

class IdReferenceControl(
        schema: SchemaWrapper<StringSchema>,
        private val idReferenceProposalProvider: IdReferenceProposalProvider,
        private val resolutionScopeProvider: ResolutionScopeProvider,
        actions: List<EditorAction>
) :
        RowBasedControl<StringSchema, String, TextField>(
                schema,
                TextField(),
                SimpleStringProperty(null),
                schema.schema.defaultValue as? String,
                listOf()
        ) {
    private val validation = ValidationSupport()
    private val formatValidator: Validator<String>?
    private val lengthValidator: Validator<String>?
    private val patternValidator: Validator<String>?
    private val referenceValidator: Validator<String>

    private var popOver: PopOver? = null

    private var description = ""
    private val textFormatter = TextFormatter<String>(this::filterChange)

    // private val previewButton: Button = node.value.action as Button

    init {
        formatValidator = StringControl.addFormatValidation(validation, control, schema.schema.formatValidator)
        lengthValidator =
                StringControl.addLengthValidation(
                        validation,
                        control,
                        schema.schema.minLength,
                        schema.schema.maxLength
                )
        val regex = schema.schema.pattern
        patternValidator = StringControl.addPatternValidation(validation, control, regex)
        referenceValidator = addReferenceValidation()

        TextFields.bindAutoCompletion(control) {
            val proposals = idReferenceProposalProvider.calcCompletionProposals(it.userText)
            if (regex != null) {
                proposals.filter { regex.matcher(it).matches() }
            } else {
                proposals
            }
        }
        control.textFormatterProperty().set(textFormatter)
        value.addListener { _, _, new -> idChanged(new) }

        editorActionsContainer.addAction(ReadOnlyAction("⤴", ActionTargetSelector.Always()) { _, rawValue ->
            val value = rawValue as? String
            if (value != null) {
                val dataAndSchema = idReferenceProposalProvider.getDataAndSchema(value)
                if (dataAndSchema != null) {
                    popOver?.hide()
                    val (data, previewSchema) = dataAndSchema
                    val preview = JsonPropertiesEditor(idReferenceProposalProvider, true, 1, resolutionScopeProvider, actions)
                    preview.display(value, control.text, data, previewSchema) { it }
                    val scrollPane = ScrollPane(preview)
                    scrollPane.maxHeight = 500.0
                    scrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                    val popOver = PopOver(addOpenButtonIfWanted(value, scrollPane))
                    popOver.minWidth = 350.0
                    popOver.maxWidth = 750.0
                    popOver.prefWidth = 350.0
                    popOver.arrowLocation = PopOver.ArrowLocation.RIGHT_TOP
                    popOver.isDetachable = true
                    popOver.title = value
                    popOver.isAnimated = false
                    popOver.root.stylesheets.add(IdReferenceControl::class.java.classLoader.getResource("unblurText.css")!!.toExternalForm())
                    popOver.show(editorActionsContainer)
                    this.popOver = popOver
                }
            }
        }.apply {
            disablePredicate = { _, value ->
                value !is String || !idReferenceProposalProvider.isValidReference(value)
            }
            description = "Open Preview for Reference Target"
        })

        actions.filter { it.matches(schema) }.forEach { editorActionsContainer.addAction(it) }

    }

    override fun bindTo(type: BindableJsonType) {
        super.bindTo(type)

        control.promptText = if (isBoundToNull()) TypeControl.NULL_PROMPT else ""
    }

    private fun addOpenButtonIfWanted(refId: String, refPane: ScrollPane) =
            if (idReferenceProposalProvider.isOpenable(refId)) {
                val openButton = Button("🖉")
                openButton.setOnAction {
                    idReferenceProposalProvider.openElement(refId)
                    popOver?.hide()
                }
                val row = HBox(openButton)
                row.alignment = Pos.CENTER
                VBox(row, refPane)
            } else {
                refPane
            }

    private fun filterChange(change: TextFormatter.Change): TextFormatter.Change? {
        if (change.controlNewText.isEmpty()) {
            description = ""
            return change
        }
        if (!change.isContentChange) {
            return change
        }
        if (!change.controlNewText.endsWith(description)) {
            if (change.isAdded || change.isReplaced) {
                val start = (value.value?.length ?: 0).coerceAtMost(change.rangeStart)
                value.value = change.controlNewText.take(start) + change.text
                return makeNopChange(change)
            } else if (change.isDeleted) {
                val start = ((value.value?.length?.dec()) ?: 0).coerceAtMost(change.rangeStart)
                value.value = change.controlNewText.take(start)
                return makeNopChange(change)
            }
        }
        value.value = change.controlNewText.dropLast(description.length)
        updateTextField()
        return makeNopChange(change)
    }

    private fun makeNopChange(change: TextFormatter.Change): TextFormatter.Change {
        change.text = ""
        change.setRange(0, 0)
        return change
    }


    private fun idChanged(id: String?) {
        if (id == null) {
            description = ""
            return
        }
        val desc = idReferenceProposalProvider.getReferenceDescription(id)
        description = if (desc.isEmpty()) {
            ""
        } else {
            " ($desc)"
        }
        updateTextField()
    }

    private fun updateTextField() {
        control.textFormatterProperty().set(null)
        control.text = (value.value ?: "") + description
        control.textFormatterProperty().set(textFormatter)
    }

    private fun referenceValidationFinished(invalid: Boolean) {
        // previewButton.isDisable = invalid
    }

    private fun addReferenceValidation(): Validator<String> {
        val validator = createReferenceValidation()
        validation.registerValidator(control, false, validator)
        return validator
    }

    private fun createReferenceValidation(): Validator<String> =
            Validator { control, _ ->
                val invalid = !idReferenceProposalProvider.isValidReference(value.value)
                referenceValidationFinished(invalid);
                ValidationResult.fromErrorIf(control, "Has to be a valid reference", invalid)
            }

}