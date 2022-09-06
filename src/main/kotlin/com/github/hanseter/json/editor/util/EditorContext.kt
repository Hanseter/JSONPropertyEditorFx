package com.github.hanseter.json.editor.util

import com.github.hanseter.json.editor.IdReferenceProposalProvider
import com.github.hanseter.json.editor.controls.TypeControl
import java.util.function.Supplier

class EditorContext(
    val refProvider: Supplier<IdReferenceProposalProvider>,
    val editorObjId: String,
    val childrenChangedCallback: (TypeControl) -> Unit,
    val idRefDisplayMode: IdRefDisplayMode,
)

