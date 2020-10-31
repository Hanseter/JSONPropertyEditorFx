package com.github.hanseter.json.editor.actions

import com.github.hanseter.json.editor.IdReferenceProposalProvider
import com.github.hanseter.json.editor.types.IdReferenceModel
import com.github.hanseter.json.editor.types.TypeModel
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.controlsfx.control.PopOver
import org.json.JSONObject

class PreviewAction(val idReferenceProposalProvider: IdReferenceProposalProvider) : EditorAction {
    override val text: String = "⤴"
    override val description: String = "Open Preview for Reference Target"
    override val selector: ActionTargetSelector = ActionTargetSelector.Always()

    private var popOver: PopOver? = null

    override fun apply(currentData: JSONObject, model: TypeModel<*>): JSONObject? {
        val value = (model as IdReferenceModel).value
        if (value != null) {
            val dataAndSchema = idReferenceProposalProvider.getDataAndSchema(value)
            if (dataAndSchema != null) {
//                showPreviewPopup(dataAndSchema, value)
            }
        }
        return null
    }

    override fun shouldBeDisabled(model: TypeModel<*>): Boolean =
            !idReferenceProposalProvider.isValidReference((model as IdReferenceModel).value)

//    private fun showPreviewPopup(dataAndSchema: IdReferenceProposalProvider.DataWithSchema, value: String) {
//        popOver?.hide()
//        val (data, previewSchema) = dataAndSchema
//        val preview = JsonPropertiesEditor(idReferenceProposalProvider, true, 1, context.resolutionScopeProvider, context.actions)
//        preview.display(value, value, data, previewSchema) { it }
//        val scrollPane = ScrollPane(preview)
//        scrollPane.maxHeight = 500.0
//        scrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
//        val popOver = PopOver(addOpenButtonIfWanted(value, scrollPane))
//        popOver.minWidth = 350.0
//        popOver.maxWidth = 750.0
//        popOver.prefWidth = 350.0
//        popOver.arrowLocation = PopOver.ArrowLocation.RIGHT_TOP
//        popOver.isDetachable = true
//        popOver.title = value
//        popOver.isAnimated = false
//        popOver.root.stylesheets.add(IdReferenceControl::class.java.classLoader.getResource("unblurText.css")!!.toExternalForm())
//        popOver.show(editorActionsContainer)
//        this.popOver = popOver
//    }

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
}