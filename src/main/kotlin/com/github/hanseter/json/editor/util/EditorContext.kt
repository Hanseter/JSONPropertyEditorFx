package com.github.hanseter.json.editor.util

import com.github.hanseter.json.editor.IdReferenceProposalProvider
import com.github.hanseter.json.editor.PropertyControlFactory
import com.github.hanseter.json.editor.ResourceProvider
import com.github.hanseter.json.editor.controls.TypeControl
import java.text.DecimalFormatSymbols
import java.util.function.Supplier

class EditorContext(
    val refProvider: () -> IdReferenceProposalProvider,
    val resourceProvider: () -> ResourceProvider,
    val editorObjId: String,
    val childrenChangedCallback: (TypeControl) -> Unit,
    val idRefDisplayMode: IdRefDisplayMode,
    val decimalFormatSymbols: DecimalFormatSymbols,
    val controlFactory: PropertyControlFactory,
    val customizationObject: () -> CustomizationObject
)

