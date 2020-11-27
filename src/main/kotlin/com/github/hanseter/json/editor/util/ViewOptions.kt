package com.github.hanseter.json.editor.util

/**
 * Options for the display of the JSON editor.
 *
 * @param markRequired Whether required properties should be marked in some way.
 * @param groupBy The grouping mechanism for the properties of an object.
 * @param collapseThreshold The amount of children a complex control can have until it will be automatically collapsed when created.
 */
data class ViewOptions(val markRequired: Boolean = false,
                       val groupBy: PropertyGrouping = PropertyGrouping.REQUIRED,
                       val collapseThreshold: Int = 5)

/**
 * The grouping mechanism for the properties of an object.
 */
enum class PropertyGrouping {
    /**
     * No ordering will be done.
     */
    NONE,

    /**
     * The properties will be split into two groups: required properties and optional properties
     */
    REQUIRED
}