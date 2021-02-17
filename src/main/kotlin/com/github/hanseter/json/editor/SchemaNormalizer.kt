package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.schemaExtensions.ColorFormat
import com.github.hanseter.json.editor.schemaExtensions.IdReferenceFormat
import org.everit.json.schema.Schema
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import java.io.InputStream
import java.io.UncheckedIOException
import java.net.URI

object SchemaNormalizer {

    fun parseSchema(
        schema: JSONObject,
        resolutionScope: URI?,
        readOnly: Boolean,
        referenceProposalProvider: IdReferenceProposalProvider
    ): Schema = SchemaLoader.builder()
        .useDefaults(true)
        .draftV7Support()
        .addFormatValidator(ColorFormat.Validator)
        .addFormatValidator(IdReferenceFormat.Validator(referenceProposalProvider))
        .schemaJson(normalizeSchema(schema, resolutionScope))
        .build().load().readOnly(readOnly).build()

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
        if (resolveRefsInAllOf(schemaPart, schema, resolutionScope, copyTarget)) return
        if (resolveRefsInOneOf(schemaPart, schema, resolutionScope, copyTarget)) return
        if (resolveRefsInProperties(schemaPart, schema, resolutionScope, copyTarget)) return
        if (resolveRefsInItems(schemaPart, schema, resolutionScope, copyTarget)) return

        val ref = schemaPart.optString("${'$'}ref", null) ?: return
        val referredSchema = resolveRefs(
            if (ref.first() == '#') {
                resolveRefInDocument(schema, ref.drop(2), resolutionScope)
            } else {
                resolveRefFromUrl(ref, resolutionScope).use {
                    JSONObject(JSONTokener(it))
                }
            }, resolutionScope
        )
        val target = copyTarget()
        target.remove("${"$"}ref")
        referredSchema.keySet().forEach {
            if (!target.has(it)) {
                target.put(it, referredSchema.get(it))
            }
        }
    }

    private fun resolveRefsInProperties(
        schemaPart: JSONObject,
        schema: JSONObject,
        resolutionScope: URI?,
        copyTarget: () -> JSONObject
    ): Boolean {
        val properties = schemaPart.optJSONObject("properties")
        if (properties != null) {
            properties.keySet().forEach { key ->
                resolveRefs(schema, properties.getJSONObject(key), resolutionScope) {
                    copyTarget().getJSONObject("properties").getJSONObject(key)
                }
            }
            return true
        }
        return false
    }

    private fun resolveRefsInItems(
        schemaPart: JSONObject,
        schema: JSONObject,
        resolutionScope: URI?,
        copyTarget: () -> JSONObject
    ): Boolean {
        val arrayItems = schemaPart.optJSONObject("items")
        if (arrayItems != null) {
            resolveRefs(schema, arrayItems, resolutionScope) {
                copyTarget().getJSONObject("items")
            }
            return true
        }
        val tupleItems = schemaPart.optJSONArray("items")
        if (tupleItems != null) {
            tupleItems.forEachIndexed { index, obj ->
                resolveRefs(schema, obj as JSONObject, resolutionScope) {
                    copyTarget().getJSONArray("items").getJSONObject(index)
                }
            }
            return true
        }
        return false
    }

    private fun resolveRefsInAllOf(
        schemaPart: JSONObject,
        schema: JSONObject,
        resolutionScope: URI?,
        copyTarget: () -> JSONObject
    ): Boolean =
        resolveRefsInComposition(schemaPart, schema, resolutionScope, copyTarget, "allOf")

    private fun resolveRefsInOneOf(
        schemaPart: JSONObject,
        schema: JSONObject,
        resolutionScope: URI?,
        copyTarget: () -> JSONObject
    ): Boolean =
        resolveRefsInComposition(schemaPart, schema, resolutionScope, copyTarget, "oneOf")

    private fun resolveRefsInComposition(
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
        inlineCompositions(schema) {
            if (copy == null) {
                copy = createCopy(schema)
            }
            copy!!
        }
        return copy ?: schema
    }

    private fun inlineCompositions(
        subPart: JSONObject,
        copyTarget: () -> JSONObject
    ) {
        if (inlineInProperties(subPart, copyTarget)) return
        if (inlineInItems(subPart, copyTarget)) return
        
        val allOf = subPart.optJSONArray("allOf") ?: return
        copyTarget().apply {
            remove("allOf")
            put("type", "object")
            put("properties", JSONObject())
        }
        getAllPropertiesInAllOf(allOf).forEach { propObj ->
            merge(copyTarget().getJSONObject("properties"), propObj.getJSONObject("properties"))
        }
        inlineCompositions(copyTarget()) { copyTarget() }
    }

    private fun inlineInProperties(subPart: JSONObject, copyTarget: () -> JSONObject): Boolean {
        val properties = subPart.optJSONObject("properties")
        if (properties != null) {
            properties.keySet().forEach {
                inlineCompositions(properties.getJSONObject(it)) {
                    copyTarget().getJSONObject("properties").getJSONObject(it)
                }
            }
            return true
        }
        return false
    }

    private fun inlineInItems(subPart: JSONObject, copyTarget: () -> JSONObject): Boolean {
        val arrItems = subPart.optJSONObject("items")
        if (arrItems != null) {
            inlineCompositions(arrItems) {
                copyTarget().getJSONObject("items")
            }
            return true
        }
        val tupleItems = subPart.optJSONArray("items")
        if (tupleItems != null) {
            tupleItems.forEachIndexed { index, obj ->
                inlineCompositions(obj as JSONObject) {
                    copyTarget().getJSONArray("items").getJSONObject(index)
                }
            }
            return true
        }
        return false
    }

    private fun getAllPropertiesInAllOf(allOf: JSONArray): List<JSONObject> {
        return (0 until allOf.length()).flatMap { i ->
            val allOfEntry = allOf.getJSONObject(i)
            val props = allOfEntry.optJSONObject("properties")
            if (props != null) {
                listOf(allOfEntry)
            } else {
                val nestedAllOff = allOfEntry.optJSONArray("allOf")
                if (nestedAllOff != null) {
                    getAllPropertiesInAllOf(nestedAllOff)
                } else {
                    emptyList()
                }
            }
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