package com.github.hanseter.json.editor.controls

interface ObjectControl : TypeControl {
    val requiredChildren: List<TypeControl>
    val optionalChildren: List<TypeControl>

    val allRequiredChildren: List<TypeControl>
    val allOptionalChildren: List<TypeControl>

    override val childControls: List<TypeControl>
        get() = requiredChildren + optionalChildren
}