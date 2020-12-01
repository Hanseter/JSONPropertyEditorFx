package com.github.hanseter.json.editor.util

import javafx.scene.Node

interface LazyControl {
    val control: Node
    fun updateDisplayedValue()
}