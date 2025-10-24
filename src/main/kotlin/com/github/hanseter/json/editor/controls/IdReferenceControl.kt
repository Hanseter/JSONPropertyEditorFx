package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.ui.LabelledTextField
import com.github.hanseter.json.editor.util.EditorContext
import com.github.hanseter.json.editor.util.IdRefDisplayMode
import javafx.application.Platform
import javafx.beans.property.Property
import javafx.beans.property.SimpleStringProperty
import org.controlsfx.control.textfield.AutoCompletionBinding
import org.controlsfx.control.textfield.TextFields
import org.everit.json.schema.StringSchema
import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.stream.Stream

class IdReferenceControl(
    private val schema: EffectiveSchema<StringSchema>,
    private val context: EditorContext,
) : ControlWithProperty<String?> {
    override val control = LabelledTextField()
    override val property: Property<String?> = SimpleStringProperty("")

    init {
        val regex = schema.baseSchema.pattern
        var internalChange = false
        val completionBinding = TextFields.bindAutoCompletion(control) { request ->
            val proposals = mapProposals(getValidProposals(request, regex))
                .filter { it.matchesInput(request.userText) }
                .limit(30)
                .sorted(Comparator.comparingInt { it.toString().indexOf(request.userText) })
                .toList()
            if (canProposalBeAutoApplied(request, proposals)) {
                Platform.runLater {
                    idChanged(proposals.single())
                }
                emptyList()
            } else proposals
        }
        completionBinding.setOnAutoCompleted {
            it.consume()
            internalChange = true
            idChanged(it.completion)
            internalChange = false
            control.end()
        }

        property.addListener { _, _, new ->
            if (internalChange) return@addListener
            idChanged(new?.let {
                Preview(
                    new,
                    context.refProvider.get()
                        .getReferenceDescription(new, context.editorObjId, schema.baseSchema)
                )
            })
        }
        control.textProperty().addListener { _, _, new ->
            internalChange = true
            idChanged(new?.let {
                Preview(
                    new,
                    context.refProvider.get()
                        .getReferenceDescription(new, context.editorObjId, schema.baseSchema)
                )
            })
            internalChange = false
        }
    }

    private fun canProposalBeAutoApplied(
        request: AutoCompletionBinding.ISuggestionRequest,
        proposals: List<Preview>,
    ) = proposals.size == 1 && proposals.single()
        .equalsInput(request.userText)

    private fun mapProposals(proposals: Stream<String>): Stream<Preview> =
        when (context.idRefDisplayMode) {
            IdRefDisplayMode.ID_ONLY -> proposals.map { Preview(it, "") }
            else -> proposals
                .map {
                    Preview(
                        it,
                        context.refProvider.get()
                            .getReferenceDescription(it, context.editorObjId, schema.baseSchema)
                    )
                }
        }

    private fun getValidProposals(
        request: AutoCompletionBinding.ISuggestionRequest,
        regex: Pattern?,
    ): Stream<String> {
        val proposals = context.refProvider.get().calcCompletionProposals(
            request.userText,
            context.editorObjId,
            schema.baseSchema,
            context.idRefDisplayMode
        )
        return if (regex != null) proposals.filter { regex.matcher(it).matches() }
        else proposals
    }

    private fun idChanged(id: Preview?) {
        if (id == null) {
            updateText("")
            control.label = ""
            property.value = null
            return
        }
        when (context.idRefDisplayMode) {
            IdRefDisplayMode.ID_ONLY -> {
                updateText(id.id)
                control.label = ""
                property.value = id.id
            }

            IdRefDisplayMode.DESCRIPTION_ONLY -> {
                if (id.description == null) {
                    updateText(id.id)
                    control.label = ""
                    property.value = ""
                } else {
                    updateText(id.description)
                    control.label = ""
                    property.value = id.id
                }
            }

            IdRefDisplayMode.ID_WITH_DESCRIPTION -> {
                updateText(id.id)
                control.label = id.description ?: ""
                property.value = id.id
            }

            IdRefDisplayMode.DESCRIPTION_WITH_ID -> {
                if (id.description == null) {
                    updateText(id.id)
                    control.label = ""
                    property.value = ""
                } else {
                    updateText(id.description)
                    control.label = id.id
                    property.value = id.id
                }
            }
        }
    }

    //the caret position listens to the invalidation listener of the text and not the change listener
    //this leads to the caret sometimes jumping to the front for no good reason
    private fun updateText(text: String) {
        if (control.text != text) control.text = text
    }

    override fun previewNull(b: Boolean) {
        control.promptText = if (b) TypeControl.NULL_PROMPT else ""
    }

    inner class Preview(val id: String, description: String) {
        val description: String? = if (description.isBlank()) null
        else description

        fun matchesInput(input: String) =
            toString().contains(input, ignoreCase = true)

        fun equalsInput(input: String) =
            id == input || description?.equals(input) ?: false

        override fun toString(): String = when (context.idRefDisplayMode) {
            IdRefDisplayMode.ID_ONLY -> id
            IdRefDisplayMode.DESCRIPTION_ONLY -> description ?: id
            IdRefDisplayMode.ID_WITH_DESCRIPTION -> description?.let { "$id ($description)" } ?: id
            IdRefDisplayMode.DESCRIPTION_WITH_ID -> description?.let { "$description ($id)" } ?: id
        }
    }
}