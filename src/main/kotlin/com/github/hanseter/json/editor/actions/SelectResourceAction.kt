package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.PropertiesEditInput
import com.github.hanseter.json.editor.PropertiesEditResult
import com.github.hanseter.json.editor.ResourceProvider
import com.github.hanseter.json.editor.actions.EditorAction.Companion.createTextIcon
import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import com.github.hanseter.json.editor.types.ResourceModel
import com.github.hanseter.json.editor.types.SupportedType
import com.github.hanseter.json.editor.types.TypeModel
import javafx.event.Event
import javafx.scene.Node
import javafx.stage.FileChooser
import javafx.stage.Window
import java.util.function.Supplier

class SelectResourceAction(
    private val windowProvider: Supplier<Window?>,
    private val resourceProvider: Supplier<ResourceProvider>
) :
    EditorAction {
    override val description: String
        get() = JsonPropertiesMl.bundle.getString("jsonEditor.actions.selectResource")
    override val selector: TargetSelector
        get() = if (resourceProvider.get().resourceSelector != null)
            TargetSelector.SchemaType(SupportedType.SimpleType.ResourceType)
        else TargetSelector.Never

    override fun createIcon(size: Int): Node = createTextIcon("🗁", size)

    override fun apply(
        input: PropertiesEditInput,
        model: TypeModel<*, *>,
        mouseEvent: Event?
    ): PropertiesEditResult? {
        if (model !is ResourceModel) return null
        val provider = resourceProvider.get()
        val selector = provider.resourceSelector ?: return null
        val file = FileChooser().apply {
            selector.getInitialDirectory(model.schema)?.also { initialDirectory = it }
            extensionFilters += FileChooser.ExtensionFilter(
                JsonPropertiesMl.bundle.getString("jsonEditor.actions.resource.desc"),
                provider.getValidExtensions(model.schema).map { "*.$it" }.ifEmpty { listOf("*.*") })
        }.showOpenDialog(windowProvider.get()) ?: return null

        val change = selector.resourceSelected(model.schema, file) ?: return null

        return ResetToNullAction.applyChange(model, input, change)
    }
}