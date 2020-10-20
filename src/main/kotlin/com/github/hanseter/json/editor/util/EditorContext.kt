package com.github.hanseter.json.editor.util

import com.github.hanseter.json.editor.IdReferenceProposalProvider
import com.github.hanseter.json.editor.ResolutionScopeProvider
import com.github.hanseter.json.editor.actions.ActionsContainer
import com.github.hanseter.json.editor.actions.EditorAction
import com.github.hanseter.json.editor.controls.TypeControl

class EditorContext(val refProvider: IdReferenceProposalProvider,
                    val resolutionScopeProvider: ResolutionScopeProvider,
                    val actions: List<EditorAction>,
                    private val executeActionCallback: (EditorAction, TypeControl) -> Unit) {

    fun createActionContainer(control: TypeControl): ActionsContainer =
            ActionsContainer(control, actions, executeActionCallback)
}

