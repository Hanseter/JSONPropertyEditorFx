package com.github.hanseter.json.editor.schemaExtensions

import org.everit.json.schema.CombinedSchema
import org.everit.json.schema.NullSchema
import java.lang.reflect.Method

private val isSyntheticMethod: Method =
    CombinedSchema::class.java.getDeclaredMethod("isSynthetic").apply { isAccessible = true }

val CombinedSchema.isNullableSchema: Boolean
    get() = this.criterion == CombinedSchema.ANY_CRITERION
            && this.subschemas.size == 2
            && this.subschemas.any { it is NullSchema }

val CombinedSchema.synthetic: Boolean
    get() = isNullableSchema || isSyntheticMethod.invoke(this) as Boolean