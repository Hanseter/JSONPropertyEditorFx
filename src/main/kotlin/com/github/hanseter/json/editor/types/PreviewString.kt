/*
 * SWEETS - Software Engineering Tooling Suite
 *
 * Copyright (c) Siemens Mobility GmbH 2022, All Rights Reserved, Confidential.
 */
package com.github.hanseter.json.editor.types

import com.github.hanseter.json.editor.i18n.JsonPropertiesMl

/**
 *
 * @author Henrik Frühling (henrik.fruehling@siemens.com)
 */
class PreviewString @JvmOverloads constructor(
    val string: String,
    val isDefaultValue: Boolean = false,
    val isPseudoValue: Boolean = false,
) {
    companion object {
        val NO_VALUE: PreviewString = PreviewString(JsonPropertiesMl.bundle.getString("jsonEditor.missing"), isPseudoValue = true)
    }
}