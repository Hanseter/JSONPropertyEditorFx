package com.github.hanseter.json.editor

import org.json.JSONObject


data class JsonEditorData @JvmOverloads constructor(
        val data: JSONObject,
        val schema: JSONObject? = null
)
