package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.IdReferenceProposalProvider
import com.github.hanseter.json.editor.JsonPropertiesEditor
import com.github.hanseter.json.editor.ResolutionScopeProvider
import com.github.hanseter.json.editor.types.SupportedType
import com.github.hanseter.json.editor.types.TypeModel
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.ScrollPane
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Modality
import org.json.JSONObject

class PreviewAction(private val idReferenceProposalProvider: IdReferenceProposalProvider,
                    private val resolutionScopeProvider: ResolutionScopeProvider) : EditorAction {
    override val text: String = "⤴"
    override val description: String = "Open Preview for Reference Target"
    override val selector: ActionTargetSelector = ActionTargetSelector.SchemaType(SupportedType.SimpleType.IdReferenceType)

    override fun apply(currentData: JSONObject, model: TypeModel<*, *>): JSONObject? {
        val value = (model as TypeModel<String?, SupportedType.SimpleType.IdReferenceType>).value
        if (value != null) {
            val dataAndSchema = idReferenceProposalProvider.getDataAndSchema(value)
            if (dataAndSchema != null) {
                showPreviewPopup(dataAndSchema, value)
            }
        }
        return null
    }

    override fun shouldBeDisabled(model: TypeModel<*, *>): Boolean =
            !idReferenceProposalProvider.isValidReference((model as TypeModel<String?, SupportedType.SimpleType.IdReferenceType>).value)

    private fun showPreviewPopup(dataAndSchema: IdReferenceProposalProvider.DataWithSchema, value: String) {
        val (data, previewSchema) = dataAndSchema
        val preview = JsonPropertiesEditor(idReferenceProposalProvider, true, 1, resolutionScopeProvider)
        preview.display(value, value, data, previewSchema) { it }
        val scrollPane = ScrollPane(preview)
        scrollPane.maxHeight = 500.0
        scrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        val popOver = Dialog<Unit>()
        popOver.dialogPane.buttonTypes.setAll(ButtonType.CANCEL)
        popOver.dialogPane.lookupButton(ButtonType.CANCEL)?.also {
            it.isManaged = false
            it.isVisible = false
        }
        popOver.initModality(Modality.NONE)
        popOver.dialogPane.addEventHandler(KeyEvent.KEY_RELEASED) {
            if (KeyCode.ESCAPE == it.code) {
                popOver.hide()
            }
        }
        popOver.dialogPane.content = addOpenButtonIfWanted(popOver, value, scrollPane)
        popOver.title = value
        popOver.show()
    }

    private fun addOpenButtonIfWanted(dialog: Dialog<*>, refId: String, refPane: ScrollPane) =
            if (idReferenceProposalProvider.isOpenable(refId)) {
                val openButton = Button("🖉")
                openButton.setOnAction {
                    idReferenceProposalProvider.openElement(refId)
                    dialog.hide()
                }
                val row = HBox(openButton)
                row.alignment = Pos.CENTER
                VBox(row, refPane)
            } else {
                refPane
            }
}