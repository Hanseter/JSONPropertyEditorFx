package com.github.hanseter.json.editor.util

import com.sun.javafx.scene.control.skin.LabelSkin
import javafx.scene.control.Label
import org.controlsfx.control.decoration.Decorator

/**
 * Skin for labels that preserves any decorations added to it.
 *
 * This should be set as soon as possible after the label's construction for maximum effect.
 */
class DecoratableLabelSkin(label: Label) : LabelSkin(label) {
    override fun updateChildren() {
        val decorations = Decorator.getDecorations(skinnable)?.toList() ?: listOf()
        Decorator.removeAllDecorations(skinnable)

        super.updateChildren()

        decorations.forEach {
            Decorator.addDecoration(skinnable, it)
        }
    }
}