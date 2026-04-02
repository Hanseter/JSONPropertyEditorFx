package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.util.EditorContext
import javafx.application.Platform
import javafx.beans.property.Property
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TextField
import org.controlsfx.control.textfield.AutoCompletionBinding
import org.controlsfx.control.textfield.TextFields
import org.everit.json.schema.StringSchema
import java.util.regex.Pattern
import java.util.stream.Stream

class ResourceControl(
    private val schema: EffectiveSchema<StringSchema>,
    private val context: EditorContext,
) : ControlWithProperty<String?> {
    override val control = TextField()
    override val property: Property<String?> = SimpleStringProperty("")

    init {
        val regex = schema.baseSchema.pattern
        var internalChange = false

        val completionBinding = TextFields.bindAutoCompletion(control) { request ->
            val comparator: Comparator<String> =
                Comparator.comparingInt<String> { it.indexOf(request.userText, 0, true) }
                    .thenBy { it }
            val proposals = getValidProposals(request, regex)
                .limit(30)
                .sorted(comparator)
                .toList()


            if (canProposalBeAutoApplied(request, proposals)) {
                Platform.runLater {
                    resourceChanged(proposals.single())
                }
                emptyList()
            } else proposals
        }
        completionBinding.setOnAutoCompleted {
            it.consume()
            internalChange = true
            resourceChanged(it.completion)
            internalChange = false
            control.end()
        }

        property.addListener { _, _, new ->
            if (internalChange) return@addListener
            resourceChanged(new)
        }
        control.textProperty().addListener { _, _, new ->
            internalChange = true
            resourceChanged(new)
            internalChange = false
        }
    }

    private fun canProposalBeAutoApplied(
        request: AutoCompletionBinding.ISuggestionRequest,
        proposals: List<String>,
    ) = proposals.size == 1 && proposals.single() == request.userText

    private fun getValidProposals(
        request: AutoCompletionBinding.ISuggestionRequest,
        regex: Pattern?,
    ): Stream<String> {
        val proposals = context.resourceProvider.get().getMatchingResources(
            request.userText,
            context.editorObjId,
            schema,
        )
        val extensions = context.resourceProvider.get().getValidExtensions(schema)
        val filtered = if (regex != null) proposals.filter { regex.matcher(it).matches() }
        else proposals
        return filtered.filter { proposal ->
            (extensions.isEmpty() || extensions.any { proposal.endsWith(it, true) }) &&
                    proposal.contains(request.userText, ignoreCase = true)
        }
    }

    private fun resourceChanged(resource: String?) {
        if (resource == null) {
            updateText("")
            property.value = null
            return
        }
        updateText(resource)
        property.value = resource
    }

    //the caret position listens to the invalidation listener of the text and not the change listener
    //this leads to the caret sometimes jumping to the front for no good reason
    private fun updateText(text: String) {
        if (control.text != text) control.text = text
    }

    override fun previewNull(b: Boolean) {
        control.promptText = if (b) TypeControl.NULL_PROMPT else ""
    }

}