package com.github.hanseter.json.editor.controls

import com.github.hanseter.json.editor.extensions.EffectiveSchema
import com.github.hanseter.json.editor.util.EditorContext
import com.github.hanseter.json.editor.util.LabelledTextField
import javafx.beans.property.Property
import org.controlsfx.control.textfield.TextFields
import org.everit.json.schema.StringSchema

class IdReferenceControl(schema: EffectiveSchema<StringSchema>, private val context: EditorContext) : ControlWithProperty<String?> {
    override val control = LabelledTextField()
    override val property: Property<String?> = control.textProperty()

    init {
        val regex = schema.baseSchema.pattern
        TextFields.bindAutoCompletion(control) { request ->
            val proposals = context.refProvider.calcCompletionProposals(request.userText)
            if (regex != null) {
                proposals.filter { regex.matcher(it).matches() }
            } else {
                proposals
            }
        }
        property.addListener { _, _, new -> idChanged(new) }
    }

    private fun idChanged(id: String?) {
        control.text = property.value
        if (id == null) {
            control.label = ""
            return
        }
        val desc = context.refProvider.getReferenceDescription(id)
        control.label = if (desc.isBlank()) "" else " ($desc)"
    }

    override fun previewNull(b: Boolean) {
        control.promptText = if (b) TypeControl.NULL_PROMPT else ""
    }
}