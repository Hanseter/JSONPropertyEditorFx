package com.github.hanseter.json.editor.extensions

import org.everit.json.schema.Schema

class SchemaWrapper<T : Schema>(val parent: SchemaWrapper<*>?, val schema: T, customTitle: String? = null) {

	val title = customTitle ?: schema.getTitle() ?: getPropertyName()

	val readOnly: Boolean = (parent?.readOnly ?: false) || (schema.isReadOnly() ?: false)

	fun getPropertyName(): String = schema.getSchemaLocation().drop(1).split('/').last()

}

