package com.github.hanseter.json.editor.controls

import javafx.scene.control.TextField
import org.everit.json.schema.StringSchema
import org.controlsfx.validation.ValidationSupport
import org.everit.json.schema.FormatValidator
import org.controlsfx.validation.ValidationResult
import org.controlsfx.validation.Validator
import java.util.regex.Pattern
import org.controlsfx.validation.Severity
import org.json.JSONObject
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.beans.property.Property
import com.github.hanseter.json.editor.IdReferenceProposalProvider
import org.controlsfx.control.textfield.TextFields
import com.github.hanseter.json.editor.extensions.SchemaWrapper
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TextFormatter
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import org.controlsfx.control.PopOver
import javafx.scene.control.ScrollPane
import com.github.hanseter.json.editor.JsonPropertiesPane
import com.github.hanseter.json.editor.JsonPropertiesEditor
import javafx.application.Platform
import javafx.stage.PopupWindow
import javafx.scene.control.Button
import javafx.event.ActionEvent

class IdReferenceControl(
	schema: SchemaWrapper<StringSchema>,
	private val idReferenceProposalProvider: IdReferenceProposalProvider
) :
	RowBasedControl<StringSchema, String, TextField>(
		schema,
		TextField(),
		SimpleStringProperty(""),
		schema.schema.getDefaultValue() as? String ?: ""
	),
	ChangeListener<String> {
	private val validation = ValidationSupport()
	private val formatValidator: Validator<String>?
	private val lengthValidator: Validator<String>?
	private val patternValidator: Validator<String>?
	private val referenceValidator: Validator<String>

	private var description = ""
	private val textFormatter = TextFormatter<String>(this::filterChange)

	private val previewButton = Button("⤴")

	init {
		formatValidator = StringControl.addFormatValidation(validation, control, schema.schema.getFormatValidator())
		lengthValidator =
			StringControl.addLengthValidation(
				validation,
				control,
				schema.schema.getMinLength(),
				schema.schema.getMaxLength()
			)
		val regex = schema.schema.pattern
		patternValidator = StringControl.addPatternValidation(validation, control, regex)
		referenceValidator = addReferenceValidation()

		TextFields.bindAutoCompletion(control) {
			val proposals = idReferenceProposalProvider.calcCompletionProposals(it.getUserText())
			if (regex != null) {
				proposals.filter { regex.matcher(it).matches() }
			} else {
				proposals
			}
		}
		control.textFormatterProperty().set(textFormatter)
		value.addListener { _, _, new -> idChanged(new) }
		val pos = node.children.indexOf(control)
		node.children.add(pos, previewButton)
		previewButton.onAction = EventHandler<ActionEvent>({
			val dataAndSchema = idReferenceProposalProvider.getDataAndSchema(value.value)
			if (dataAndSchema != null) {
				val (data, previewSchema) = dataAndSchema
				val preview = JsonPropertiesEditor(idReferenceProposalProvider, true, 1)
				preview.display(value.value, control.text, data, previewSchema, null, { it })
				val scrollPane = ScrollPane(preview)
				scrollPane.setMaxHeight(500.0)
				scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER)
				val popOver = PopOver(scrollPane);
				popOver.setArrowLocation(PopOver.ArrowLocation.RIGHT_TOP)
				popOver.setDetachable(true);
				popOver.setTitle(value.value);
				popOver.setAnimated(false);
				popOver.getRoot().getStylesheets()
					.add(IdReferenceControl::class.java.classLoader.getResource("unblurText.css").toExternalForm());
				popOver.show(previewButton)
			}
		})
	}

	private fun filterChange(change: TextFormatter.Change): TextFormatter.Change? {
		if (change.getControlNewText().isEmpty()) {
			description = ""
			return change
		}
		if (!change.isContentChange()) {
			return change
		}
		if (!change.getControlNewText().endsWith(description)) {
			if (change.isAdded || change.isReplaced()) {
				val start = Math.min(value.value.length, change.rangeStart)
				value.value = change.controlNewText.take(start) + change.text
				return makeNopChange(change)
			} else if (change.isDeleted()) {
				val start = Math.min(value.value.length - 1, change.rangeStart)
				value.value = change.controlNewText.take(start)
				return makeNopChange(change)
			}
		}
		value.value = change.controlNewText.dropLast(description.length)
		updateTextField()
		return makeNopChange(change)
	}

	private fun makeNopChange(change: TextFormatter.Change): TextFormatter.Change {
		change.setText("")
		change.setRange(0, 0)
		return change
	}


	private fun idChanged(id: String) {
		val desc = idReferenceProposalProvider.getReferenceDesciption(id)
		if (desc.isEmpty()) {
			description = ""
		} else {
			description = " (" + desc + ")"
		}
		updateTextField()
	}

	private fun updateTextField() {
		control.textFormatterProperty().set(null)
		control.text = value.value + description
		control.textFormatterProperty().set(textFormatter)
	}

	private fun referenceValidationFinished(invalid: Boolean) {
		previewButton.setDisable(invalid)
	}

	private fun addReferenceValidation(): Validator<String> {
		val validator = createReferenceValidation()
		validation.registerValidator(control, validator)
		return validator
	}

	private fun createReferenceValidation(): Validator<String> =
		Validator({ control, _ ->
			val invalid = !idReferenceProposalProvider.isValidReference(value.value)
			referenceValidationFinished(invalid);
			ValidationResult.fromErrorIf(control, "Has to be a valid reference", invalid)
		})

}