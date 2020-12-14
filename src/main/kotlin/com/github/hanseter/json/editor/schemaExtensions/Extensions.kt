package com.github.hanseter.json.editor.schemaExtensions

import org.everit.json.schema.CombinedSchema
import java.lang.reflect.Method

private val isSyntheticMethod: Method = CombinedSchema::class.java.getDeclaredMethod("isSynthetic").apply { isAccessible = true }

val CombinedSchema.synthetic: Boolean
    get() = isSyntheticMethod.invoke(this) as Boolean