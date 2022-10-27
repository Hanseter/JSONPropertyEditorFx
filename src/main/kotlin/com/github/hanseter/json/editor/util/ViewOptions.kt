package com.github.hanseter.json.editor.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

/**
 * Options for the display of the JSON editor.
 *
 * @param markRequired Whether required properties should be marked in some way.
 * @param groupBy The grouping mechanism for the properties of an object.
 * @param collapseThreshold The amount of children a complex control can have until it will be automatically collapsed when created.
 */
data class ViewOptions @JvmOverloads constructor(
    val markRequired: Boolean = false,
    val groupBy: PropertyGrouping = PropertyGrouping.REQUIRED,
    val numberOfInitiallyOpenedObjects: Int = 5,
    val collapseThreshold: Int = 5,
    val idRefDisplayMode: IdRefDisplayMode = IdRefDisplayMode.ID_WITH_DESCRIPTION,
    val decimalFormatSymbols: DecimalFormatSymbols = DecimalFormat().decimalFormatSymbols
)

/**
 * The data that will be displayed in the id reference control.
 */
enum class IdRefDisplayMode {
    /**
     * Only the id will be displayed
     */
    ID_ONLY,

    /**
     * Only the description will be displayed
     */
    DESCRIPTION_ONLY,

    /**
     * The id will be displayed with the description in parentheses
     */
    ID_WITH_DESCRIPTION,

    /**
     * The description will be displayed with the id in parentheses
     */
    DESCRIPTION_WITH_ID

}

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