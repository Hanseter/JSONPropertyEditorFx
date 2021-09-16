package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.ui.LabelledTextField
import com.github.hanseter.json.editor.util.EditorContext
import com.github.hanseter.json.editor.util.IdRefDisplayMode
import javafx.beans.property.Property
import javafx.beans.property.SimpleStringProperty
import org.controlsfx.control.textfield.AutoCompletionBinding
import org.controlsfx.control.textfield.TextFields
import org.everit.json.schema.StringSchema
import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.stream.Stream

class IdReferenceControl(private val schema: EffectiveSchema<StringSchema>, private val context: EditorContext) : ControlWithProperty<String?> {
    override val control = LabelledTextField()
    override val property: Property<String?> = SimpleStringProperty("")//control.textProperty()

    init {
        val regex = schema.baseSchema.pattern
        var internalChange = false
        val completionBinding = TextFields.bindAutoCompletion(control) { request ->
            mapProposals(getValidProposals(request, regex))
                    .filter { it.matchesInput(request.userText) }
                    .limit(30).collect(Collectors.toList())
        }
        completionBinding.setOnAutoCompleted {
            it.consume()
            internalChange = true
            idChanged(it.completion)
            internalChange = false
        }

        property.addListener { _, _, new ->
            if (internalChange) return@addListener
            idChanged(new?.let {
                Preview(new, context.refProvider.get().getReferenceDescription(new, context.editorObjId, schema.baseSchema))
            })
        }
        control.textProperty().addListener { _, _, new ->
            internalChange = true
            idChanged(new?.let {
                Preview(new, context.refProvider.get().getReferenceDescription(new, context.editorObjId, schema.baseSchema))
            })
            internalChange = false
        }
    }

    private fun mapProposals(proposals: Stream<String>): Stream<Preview> =
            when (context.idRefDisplayMode) {
                IdRefDisplayMode.ID_ONLY -> proposals.map { Preview(it, "") }
                else -> proposals
                        .map { Preview(it, context.refProvider.get().getReferenceDescription(it, context.editorObjId, schema.baseSchema)) }
            }

    private fun getValidProposals(request: AutoCompletionBinding.ISuggestionRequest, regex: Pattern?): Stream<String> {
        val proposals = context.refProvider.get().calcCompletionProposals(request.userText, context.editorObjId, schema.baseSchema, context.idRefDisplayMode)
        return if (regex != null) proposals.filter { regex.matcher(it).matches() }
        else proposals
    }

    private fun idChanged(id: Preview?) {
        if (id == null) {
            control.label = ""
            return
        }
        when (context.idRefDisplayMode) {
            IdRefDisplayMode.ID_ONLY -> {
                control.text = id.id
                property.value = id.id
            }
            IdRefDisplayMode.DESCRIPTION_ONLY -> {
                if (id.description == null) {
                    control.text = id.id
                    property.value = ""
                } else {
                    control.text = id.description
                    property.value = id.id
                }
            }
            IdRefDisplayMode.ID_WITH_DESCRIPTION -> {
                control.text = id.id
                control.label = id.description ?: ""
                property.value = id.id
            }
            IdRefDisplayMode.DESCRIPTION_WITH_ID -> {
                if (id.description == null) {
                    control.label = ""
                    control.text = id.id
                    property.value = ""
                } else {
                    control.text = id.description
                    control.label = id.id
                    property.value = id.id
                }
            }
        }
    }

    override fun previewNull(b: Boolean) {
        control.promptText = if (b) TypeControl.NULL_PROMPT else ""
    }

    inner class Preview(val id: String, description: String) {
        val description: String? = if (description.isBlank()) null
        else description

        fun matchesInput(input: String) = id.startsWith(input) || description?.startsWith(input) ?: false

        override fun toString(): String = when (context.idRefDisplayMode) {
            IdRefDisplayMode.ID_ONLY -> id
            IdRefDisplayMode.DESCRIPTION_ONLY -> description ?: id
            IdRefDisplayMode.ID_WITH_DESCRIPTION -> description?.let { "$id ($description)" } ?: id
            IdRefDisplayMode.DESCRIPTION_WITH_ID -> description?.let { "$description ($id)" } ?: id
        }
    }
}