/*
 *
 *  * SWEETS - Software Engineering Tooling Suite
 *  *
 *  * Copyright (c) Siemens Mobility GmbH 2024, All Rights Reserved, Confidential.
 *  *
 *
 */

package com.github.hanseter.json.editor.schemaExtensions

import java.util.*

/**
 * A custom format for a multi line string. By default, the UI only allows single-line strings.
 * This way multi-line strings are allowed.
 */
object MultiLineFormat {
    const val FORMAT_NAME = "multi-line"

    object Validator : org.everit.json.schema.FormatValidator {

        override fun formatName() = FORMAT_NAME

        override fun validate(subject: String?): Optional<String> = Optional.empty()
    }
}