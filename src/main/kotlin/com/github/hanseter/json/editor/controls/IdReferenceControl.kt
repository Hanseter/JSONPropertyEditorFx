package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.IdReferenceProposalProvider
import com.github.hanseter.json.editor.JsonPropertiesEditor
import com.github.hanseter.json.editor.actions.ActionTargetSelector
import com.github.hanseter.json.editor.actions.ActionsContainer
import com.github.hanseter.json.editor.actions.EditorAction
import com.github.hanseter.json.editor.extensions.FilterableTreeItem
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import com.github.hanseter.json.editor.extensions.TreeItemData
import com.github.hanseter.json.editor.util.BindableJsonType
import com.github.hanseter.json.editor.util.EditorContext
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableBooleanValue
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
import org.json.JSONObject

class IdReferenceControl(override val schema: SchemaWrapper<StringSchema>, private val context: EditorContext) : TypeControl, ControlProvider<String> {
    override val control = TextField()
    override val value: Property<String?> = SimpleStringProperty(null)
    override val defaultValue: String?
        get() = schema.schema.defaultValue as? String
    override val editorActionsContainer: ActionsContainer =
            context.createActionContainer(this, additionalActions = listOf(PreviewAction(context.refProvider)))
    private val delegate = RowBasedControl(this)

    override val node: FilterableTreeItem<TreeItemData> = delegate.node
    override val valid: ObservableBooleanValue = SimpleBooleanProperty(true)
    private val validation = ValidationSupport()
    private val formatValidator: Validator<String>?
    private val lengthValidator: Validator<String>?
    private val patternValidator: Validator<String>?
    private val referenceValidator: Validator<String>

    private var popOver: PopOver? = null

    private var description = ""
    private val textFormatter = TextFormatter<String>(this::filterChange)

    init {
        formatValidator = StringControl.addFormatValidation(validation, control, schema.schema.formatValidator)
        lengthValidator = StringControl.addLengthValidation(
                validation, control, schema.schema.minLength, schema.schema.maxLength)
        val regex = schema.schema.pattern
        patternValidator = StringControl.addPatternValidation(validation, control, regex)
        referenceValidator = addReferenceValidation()

        TextFields.bindAutoCompletion(control) { request ->
            val proposals = context.refProvider.calcCompletionProposals(request.userText)
            if (regex != null) {
                proposals.filter { regex.matcher(it).matches() }
            } else {
                proposals
            }
        }
        control.textFormatterProperty().set(textFormatter)
        value.addListener { _, _, new -> idChanged(new) }
    }


    override fun bindTo(type: BindableJsonType) {
        delegate.bindTo(type, StringControl.STRING_CONVERTER)
        control.promptText = if (delegate.isBoundToNull()) TypeControl.NULL_PROMPT else ""
    }

    private fun showPreviewPopup(dataAndSchema: IdReferenceProposalProvider.DataWithSchema, value: String) {
        popOver?.hide()
        val (data, previewSchema) = dataAndSchema
        val preview = JsonPropertiesEditor(context.refProvider, true, 1, context.resolutionScopeProvider, context.actions)
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

    private fun addOpenButtonIfWanted(refId: String, refPane: ScrollPane) =
            if (context.refProvider.isOpenable(refId)) {
                val openButton = Button("🖉")
                openButton.setOnAction {
                    context.refProvider.openElement(refId)
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
                val start = ((value.value?.length?.dec())
                        ?: 0).coerceAtMost(change.rangeStart)
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
            updateTextField()
            return
        }
        val desc = context.refProvider.getReferenceDescription(id)
        description = if (desc.isEmpty()) ""
        else " ($desc)"
        updateTextField()
    }

    private fun updateTextField() {
        control.textFormatterProperty().set(null)
        control.text = (value.value ?: "") + description
        control.textFormatterProperty().set(textFormatter)
    }

    private fun addReferenceValidation(): Validator<String> {
        val validator = createReferenceValidation()
        validation.registerValidator(control, false, validator)
        return validator
    }

    private fun referenceValidationFinished() {
        editorActionsContainer.updateDisablement()
    }

    private fun createReferenceValidation(): Validator<String> =
            Validator { control, _ ->
                val invalid = !context.refProvider.isValidReference(value.value)
                referenceValidationFinished()
                ValidationResult.fromErrorIf(control, "Has to be a valid reference", invalid)
            }

    private inner class PreviewAction(val idReferenceProposalProvider: IdReferenceProposalProvider) : EditorAction {
        override val text: String = "⤴"
        override val description: String = "Open Preview for Reference Target"
        override val selector: ActionTargetSelector = ActionTargetSelector.Always()
        override fun apply(currentData: JSONObject, schema: SchemaWrapper<*>): JSONObject? {
            val value = schema.extractProperty(currentData) as? String
            if (value != null) {
                val dataAndSchema = idReferenceProposalProvider.getDataAndSchema(value)
                if (dataAndSchema != null) {
                    showPreviewPopup(dataAndSchema, value)
                }
            }
            return null
        }

        override fun shouldBeDisabled(schema: SchemaWrapper<*>): Boolean =
                !context.refProvider.isValidReference(value.value)
    }
}