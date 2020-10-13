package com.github.hanseter.json.editor.util

import org.json.JSONArray
import org.json.JSONObject

fun getAllJsonPointerStrings(obj: JSONObject, prefix: String): List<String> {
    val pointers = mutableListOf<String>()

    val queue = mutableListOf(QueueElement("", obj))

    fun similarToPrefix(s: String) = s.startsWith(prefix) || (s.length < prefix.length && prefix.startsWith(s))

    while (queue.isNotEmpty()) {
        val element = queue.removeFirst()

        if (element.obj != null) {
            for (key in element.obj.keySet()) {
                val thisPointer = "${element.parentPointer}/${escapeJsonPointerElement(key)}"

                if (similarToPrefix(thisPointer)) {
                    if (thisPointer.startsWith(prefix)) {
                        pointers.add(thisPointer)
                    }

                    element.obj.optJSONObject(key)?.let {
                        queue.add(0, QueueElement(thisPointer, it))
                    }
                    element.obj.optJSONArray(key)?.let {
                        queue.add(0, QueueElement(thisPointer, it))
                    }
                }
            }
        } else if (element.arr != null && element.arr.length() > 0) {
            for (index in 0 until element.arr.length()) {
                val thisPointer = "${element.parentPointer}/$index"

                if (similarToPrefix(thisPointer)) {
                    if (thisPointer.startsWith(prefix)) {
                        pointers.add(thisPointer)
                    }

                    element.arr.optJSONObject(index)?.let {
                        queue.add(0, QueueElement(thisPointer, it))
                    }
                    element.arr.optJSONArray(index)?.let {
                        queue.add(0, QueueElement(thisPointer, it))
                    }
                }
            }
        }
    }

    return pointers
}

fun isValidJsonPointer(obj: JSONObject, pointer: String): Boolean {
    if (!pointer.startsWith('/')) {
        return false
    }

    val pointerElements = pointer.substring(1).split('/')

    var currentEl: Any? = obj

    for (value in pointerElements) {

        currentEl = when (currentEl) {
            is JSONObject -> {
                if (!currentEl.has(unescapeJsonPointerElement(value))) {
                    return false
                }
                currentEl.opt(value)
            }
            is JSONArray -> {
                val valueAsInt = value.toIntOrNull() ?: return false

                if (currentEl.length() <= valueAsInt) {
                    return false
                }
                currentEl.opt(valueAsInt)
            }
            else -> {
                return false
            }
        }
    }

    return true
}

fun escapeJsonPointerElement(element: String): String {
    return element.replace("~", "~0").replace("/", "~1")
}

fun unescapeJsonPointerElement(element: String): String {
    return element.replace("~1", "/").replace("~0", "~")
}


private class QueueElement private constructor(val parentPointer: String, val obj: JSONObject?, val arr: JSONArray?) {


    constructor(parentPointer: String, obj: JSONObject) : this(parentPointer, obj, null)

    constructor(parentPointer: String, arr: JSONArray) : this(parentPointer, null, arr)
}