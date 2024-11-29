package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.*
import com.github.hanseter.json.editor.controls.IdReferenceControl
import com.github.hanseter.json.editor.i18n.JsonPropertiesMl
import com.github.hanseter.json.editor.types.IdReferenceModel
import com.github.hanseter.json.editor.types.SupportedType
import com.github.hanseter.json.editor.types.TypeModel
import com.github.hanseter.json.editor.util.ViewOptions
import javafx.event.Event
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.ScrollPane
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Modality
import org.controlsfx.control.PopOver
import org.everit.json.schema.StringSchema
import java.util.function.Supplier

class PreviewAction(
    private val idReferenceProposalProvider: Supplier<IdReferenceProposalProvider>,
    private val resolutionScopeProvider: Supplier<ResolutionScopeProvider>
) : EditorAction {
    override val text: String = "⤴"
    override val description: String = JsonPropertiesMl.bundle.getString("jsonEditor.actions.preview")
    override val selector: TargetSelector =
        TargetSelector.SchemaType(SupportedType.SimpleType.IdReferenceType)

    override fun apply(
        input: PropertiesEditInput,
        model: TypeModel<*, *>,
        mouseEvent: Event?
    ): PropertiesEditResult? {
        val value = (model as TypeModel<String?, SupportedType.SimpleType.IdReferenceType>).value
        val viewOptions = (model as IdReferenceModel).viewOptions
        if (value != null) {
            val dataAndSchema = idReferenceProposalProvider.get().getDataAndSchema(value)
            if (dataAndSchema != null && mouseEvent != null) {
                showPreviewPopup(dataAndSchema, value, mouseEvent?.target as? Node,viewOptions)
            }
        }
        return null
    }

    override fun shouldBeDisabled(model: TypeModel<*, *>, objId: String): Boolean =
        !idReferenceProposalProvider.get().isValidReference(
            (model as TypeModel<String?, SupportedType.SimpleType.IdReferenceType>).value,
            objId,
            model.schema.baseSchema as StringSchema
        )

    private fun showPreviewPopup(
        dataAndSchema: IdReferenceProposalProvider.DataWithSchema,
        value: String,
        parent: Node?,
        viewOptions: ViewOptions
    ) {
        val (data, previewSchema) = dataAndSchema
        val preview = JsonPropertiesEditor(true)
        preview.referenceProposalProvider = idReferenceProposalProvider.get()
        preview.resolutionScopeProvider = resolutionScopeProvider.get()
        preview.viewOptions= viewOptions
        preview.display(value, value, data, previewSchema) { it }
        val scrollPane = ScrollPane(preview)
        scrollPane.maxHeight = 500.0
        scrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        if (parent == null) {
            Dialog<Unit>().apply {
                dialogPane.buttonTypes.setAll(ButtonType.CANCEL)
                dialogPane.lookupButton(ButtonType.CANCEL)?.also {
                    it.isManaged = false
                    it.isVisible = false
                }
                initModality(Modality.NONE)
                dialogPane.addEventHandler(KeyEvent.KEY_RELEASED) {
                    if (KeyCode.ESCAPE == it.code) {
                        hide()
                    }
                }
                dialogPane.content = addOpenButtonIfWanted(::hide, value, scrollPane)
                title = value
            }.show()
        } else {
            PopOver().apply {
                contentNode = addOpenButtonIfWanted(::hide, value, scrollPane)
                minWidth = 350.0
                maxWidth = 750.0
                prefWidth = 350.0
                arrowLocation = PopOver.ArrowLocation.RIGHT_TOP
                isDetachable = true
                title = value
                isAnimated = false
                root.stylesheets.add(
                    IdReferenceControl::class.java.classLoader.getResource("unblurText.css")!!
                        .toExternalForm()
                )
            }.show(parent)
        }
    }

    private fun addOpenButtonIfWanted(
        hideCallback: () -> Unit,
        refId: String,
        refPane: ScrollPane
    ) =
        if (idReferenceProposalProvider.get().isOpenable(refId)) {
            val openButton = Button("🖉")
            openButton.setOnAction {
                idReferenceProposalProvider.get().openElement(refId)
                hideCallback()
            }
            val row = HBox(openButton)
            row.alignment = Pos.CENTER
            VBox(row, refPane)
        } else {
            refPane
        }
}