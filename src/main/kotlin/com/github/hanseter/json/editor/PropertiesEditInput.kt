package com.github.hanseter.json.editor

import org.json.JSONObject


data class PropertiesEditInput(val data: JSONObject, private val _schema: JSONObject) {

    val schema
        get() = SchemaNormalizer.deepCopy(_schema) as JSONObject

}