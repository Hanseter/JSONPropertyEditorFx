package com.github.hanseter.json.editor

import org.json.JSONObject


data class PropertiesEditResult @JvmOverloads constructor(
        val data: JSONObject,
        val schema: JSONObject? = null
)
