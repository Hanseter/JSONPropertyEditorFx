package com.github.hanseter.json.editor.util

data class ViewOptions(val markRequired: Boolean = false, val groupBy: PropertyGrouping = PropertyGrouping.REQUIRED)

enum class PropertyGrouping {
    NONE,
    REQUIRED
}