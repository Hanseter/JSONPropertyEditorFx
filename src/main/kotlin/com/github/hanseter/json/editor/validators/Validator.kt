package com.github.hanseter.json.editor.validators

fun interface Validator<T> {
    /**
     * Returns an error message if validation fails.
     */
    fun validate(value: T): List<String>
}