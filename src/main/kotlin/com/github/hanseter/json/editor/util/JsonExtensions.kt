package com.github.hanseter.json.editor.util

import org.json.JSONArray

fun JSONArray.shallowClone(): JSONArray {
    return JSONArray().putAll(this)
}

