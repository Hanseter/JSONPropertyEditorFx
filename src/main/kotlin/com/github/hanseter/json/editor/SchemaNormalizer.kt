package com.github.hanseter.json.editor

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import java.io.InputStream
import java.io.UncheckedIOException
import java.net.URI

object SchemaNormalizer {

    fun normalizeSchema(schema: JSONObject, resolutionScope: URI?) =
        inlineCompositions(resolveRefs(schema, resolutionScope))

    fun resolveRefs(schema: JSONObject, resolutionScope: URI?): JSONObject {
        var copy: JSONObject? = null
        resolveRefs(schema, schema, resolutionScope) {
            if (copy == null) {
                copy = createCopy(schema)
            }
            copy!!
        }
        return copy ?: schema
    }

    private fun resolveRefs(
        schema: JSONObject,
        schemaPart: JSONObject,
        resolutionScope: URI?,
        copyTarget: () -> JSONObject,
    ) {
        if (handleAllOf(schemaPart, schema, resolutionScope, copyTarget)) return

        if (handleOneOf(schemaPart, schema, resolutionScope, copyTarget)) return

        val properties = schemaPart.optJSONObject("properties")
        if (properties != null) {
            properties.keySet().forEach { key ->
                resolveRefs(schema, properties.getJSONObject(key), resolutionScope) {
                    copyTarget().getJSONObject("properties").getJSONObject(key)
                }
            }
            return
        }

        val ref = schemaPart.optString("${'$'}ref", null) ?: return
        val referredSchema = if (ref.first() == '#') {
            resolveRefInDocument(schema, ref.drop(2), resolutionScope)
        } else {
            JSONObject(JSONTokener(resolveRefFromUrl(ref, resolutionScope)))
        }
        val target = copyTarget()
        target.remove("${"$"}ref")
        referredSchema.keySet().forEach {
            if (!target.has(it)) {
                target.put(it, referredSchema.get(it))
            }
        }
    }

    private fun handleAllOf(
        schemaPart: JSONObject,
        schema: JSONObject,
        resolutionScope: URI?,
        copyTarget: () -> JSONObject
    ): Boolean =
        handleComposition(schemaPart, schema, resolutionScope, copyTarget, "allOf")

    private fun handleOneOf(
        schemaPart: JSONObject,
        schema: JSONObject,
        resolutionScope: URI?,
        copyTarget: () -> JSONObject
    ): Boolean =
        handleComposition(schemaPart, schema, resolutionScope, copyTarget, "oneOf")

    private fun handleComposition(
        schemaPart: JSONObject,
        schema: JSONObject,
        resolutionScope: URI?,
        copyTarget: () -> JSONObject,
        compositionType: String
    ): Boolean {
        val composition = schemaPart.optJSONArray(compositionType)
        if (composition != null) {
            composition.forEachIndexed { index, obj ->
                resolveRefs(schema, obj as JSONObject, resolutionScope) {
                    copyTarget().getJSONArray(compositionType).getJSONObject(index)
                }
            }
            return true
        }
        return false
    }

    private fun resolveRefInDocument(
        schema: JSONObject,
        referred: String,
        resolutionScope: URI?
    ): JSONObject {
        val pointer = referred.split('/')
        val referredSchema = queryObject(schema, pointer)
        resolveRefs(referredSchema, resolutionScope)
        return referredSchema
    }

    private fun queryObjOrArray(
        schema: JSONObject,
        pointer: List<String>
    ): Any {
        var current: Any = schema
        pointer.forEach {
            current = when (current) {
                is JSONObject -> (current as JSONObject).get(it)
                is JSONArray -> (current as JSONArray).get(it.toInt())
                else -> throw IllegalArgumentException("JSON Pointer points to child of primitive")
            }
        }
        return current
    }

    private fun queryObject(schema: JSONObject, pointer: List<String>): JSONObject =
        queryObjOrArray(schema, pointer) as JSONObject

    private fun resolveRefFromUrl(url: String, resolutionScope: URI?): InputStream {
        fun get(uri: URI): InputStream {
            val conn = uri.toURL().openConnection()
            val location = conn.getHeaderField("Location")
            return location?.let { get(URI(it)) } ?: conn.content as InputStream
        }
        if (resolutionScope != null) {
            try {
                return get(resolutionScope.resolve(url))
            } catch (e: IOException) {
                //ignore exception
            }
        }
        try {
            return get(URI(url))
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }

    }

    private fun createCopy(toCopy: JSONObject): JSONObject =
        toCopy.keySet().fold(JSONObject()) { acc, it ->
            acc.put(it, deepCopy(toCopy.get(it)))
        }

    private fun createCopy(toCopy: JSONArray): JSONArray = toCopy.fold(JSONArray()) { acc, it ->
        acc.put(deepCopy(it))
    }

    private fun deepCopy(toCopy: Any): Any = when (toCopy) {
        is JSONObject -> createCopy(toCopy)
        is JSONArray -> createCopy(toCopy)
        else -> toCopy
    }

    fun inlineCompositions(schema: JSONObject): JSONObject {
        var copy: JSONObject? = null
        inlineCompostions(schema, schema) {
            if (copy == null) {
                copy = createCopy(schema)
            }
            copy!!
        }
        return copy ?: schema
    }

    private fun inlineCompostions(
        schema: JSONObject,
        subPart: JSONObject,
        copyTarget: () -> JSONObject
    ) {
        val properties = subPart.optJSONObject("properties")
        if (properties != null) {
            properties.keySet().forEach {
                inlineCompostions(schema, properties.getJSONObject(it)) {
                    copyTarget().getJSONObject("properties")
                }
            }
            return
        }
        val allOf = subPart.optJSONArray("allOf") ?: return
        subPart.remove("allOf")
        (0 until allOf.length()).fold(copyTarget()) { target, index ->
            merge(target, allOf.getJSONObject(index))
        }
    }

    fun merge(a: JSONObject, b: JSONObject) = mergeInternal(a, b)

    private fun mergeInternal(target: JSONObject, source: JSONObject): JSONObject =
        source.keySet().fold(target) { acc, key ->
            val old = acc.optJSONObject(key)
            val new = source.optJSONObject(key)
            if (old == null || new == null) {
                val oldArray = acc.optJSONArray(key)
                val newArray = source.optJSONArray(key)

                if (newArray != null) {
                    acc.put(key, mergeArrays(oldArray, newArray))
                } else {
                    acc.put(key, source.get(key))
                }
            } else {
                val merged = merge(old, new)
                acc.put(key, merged)
            }
            acc
        }

    private fun mergeArrays(target: JSONArray?, source: JSONArray): JSONArray {
        if (target == null) return source
        source.forEach {
            target.put(it)
        }
        return target
    }
}