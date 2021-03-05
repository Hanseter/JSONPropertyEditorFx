package com.github.hanseter.json.editor.util

import com.github.hanseter.json.editor.IdReferenceProposalProvider
import com.github.hanseter.json.editor.controls.TypeControl

class EditorContext(val refProvider: IdReferenceProposalProvider,
                    val editorObjId: String,
                    val childrenChangedCallback: (TypeControl) -> Unit,
                    val idRefDisplayMode: IdRefDisplayMode)

