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
import java.io.InputStreamReader
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
            convertOrder(inlineCompositions(resolveRefs(schema, resolutionScope)))

    /**
     * Resolves $refs in a schema.
     *
     * @param schema the schema to resolve the references in
     * @param resolutionScope the URI to resolve $refs to other files from
     * @param completeSchema the schema to resolve internal $refs relative to (usually the root schema), or `null` to resolve them relative to `schema`
     * @return a schema where the $refs have been resolved
     */
    fun resolveRefs(schema: JSONObject, resolutionScope: URI?, completeSchema: JSONObject? = null): JSONObject {
        var copy: JSONObject? = null
        resolveRefs(completeSchema ?: schema, schema, resolutionScope) {
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
        if (resolveRefsInAdditionalProperties(schemaPart, schema, resolutionScope, copyTarget)) return
        if (resolveRefsInItems(schemaPart, schema, resolutionScope, copyTarget)) return

        val ref = schemaPart.optString("\$ref", null) ?: return

        val referredSchema = if (ref.first() == '#') {
            resolveRefs(
                    resolveRefInDocument(schema, ref.drop(2), resolutionScope),
                    resolutionScope, schema
            )
        } else {
            resolveRefs(
                    resolveRefFromUrl(ref, resolutionScope).use {
                        JSONObject(JSONTokener(it.reader(Charsets.UTF_8)))
                    },
                    resolutionScope
            )
        }

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

    private fun resolveRefsInAdditionalProperties(
            schemaPart: JSONObject,
            schema: JSONObject,
            resolutionScope: URI?,
            copyTarget: () -> JSONObject
    ): Boolean {
        val properties = schemaPart.optJSONObject("additionalProperties")
        if (properties != null) {
            resolveRefs(schema, properties, resolutionScope) {
                copyTarget().getJSONObject("additionalProperties")
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
                if (obj is JSONObject) {
                    resolveRefs(schema, obj as JSONObject, resolutionScope) {
                        copyTarget().getJSONArray(compositionType).getJSONObject(index)
                    }
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
        resolveRefs(referredSchema, resolutionScope, schema)
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

    fun deepCopy(toCopy: Any): Any = when (toCopy) {
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

    private tailrec fun inlineCompositions(
            subPart: JSONObject,
            copyTarget: () -> JSONObject
    ) {
        inlineInValidationOnly(subPart, copyTarget)

        if (inlineInProperties(subPart, copyTarget)) return
        if (inlineInAdditionalProperties(subPart, copyTarget)) return
        if (inlineInItems(subPart, copyTarget)) return

        val allOf = subPart.optJSONArray("allOf") ?: return
        copyTarget().apply {
            remove("allOf")
            put("type", "object")
            put("properties", JSONObject())
        }
        val order = JSONArray()
        getAllObjectsInAllOf(allOf).forEach { propObj ->
            merge(copyTarget().getJSONObject("properties"), propObj.getJSONObject("properties"))
            propObj.optJSONArray("order")?.also { order.put(it) }
            propObj.optJSONObject("order")?.also { order.put(it) }
            merge(copyTarget(), propObj, copyTarget().keySet())
        }
        if (!order.isEmpty) {
            copyTarget().put("order", order)
        }

        inlineCompositions(copyTarget(), copyTarget)
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

    private fun inlineInAdditionalProperties(subPart: JSONObject, copyTarget: () -> JSONObject): Boolean {
        val properties = subPart.optJSONObject("additionalProperties")
        if (properties != null) {
            inlineCompositions(properties) {
                copyTarget().getJSONObject("additionalProperties")
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

    private fun inlineInValidationOnly(subPart: JSONObject, copyTarget: () -> JSONObject) {
        inlineCompositionsForKey("not", subPart, copyTarget)
        inlineCompositionsForKey("if", subPart, copyTarget)
        inlineCompositionsForKey("then", subPart, copyTarget)
        inlineCompositionsForKey("else", subPart, copyTarget)
    }

    private fun inlineCompositionsForKey(key: String, subPart: JSONObject, copyTarget: () -> JSONObject) {
        subPart.optJSONObject(key)?.let { ifSchema ->
            inlineCompositions(ifSchema) {
                copyTarget().getJSONObject(key)
            }
        }
    }

    /**
     * Gets all objects inside a JSONArray.
     * If the array contains items other than objects, they are filtered out.
     */
    private fun getAllObjectsInAllOf(allOf: JSONArray): List<JSONObject> {
        return (0 until allOf.length()).flatMap { i ->
            val allOfEntry = allOf.get(i)
            if (allOfEntry is JSONObject) {
                val props = allOfEntry.optJSONObject("properties")
                if (props != null) {
                    listOf(allOfEntry)
                } else {
                    val nestedAllOff = allOfEntry.optJSONArray("allOf")
                    if (nestedAllOff != null) {
                        getAllObjectsInAllOf(nestedAllOff)
                    } else {
                        emptyList()
                    }
                }
            } else {
                emptyList()
            }
        }
    }

    fun convertOrder(schema: JSONObject): JSONObject {
        var copy: JSONObject? = null
        convertOrder(schema) {
            if (copy == null) {
                copy = createCopy(schema)
            }
            copy!!
        }
        return copy ?: schema
    }

    private fun convertOrder(schema: JSONObject, copyProvider: () -> JSONObject) {
        convertOrderInProperties(schema, copyProvider)
        convertOrderInItems(schema, copyProvider)
        val target = copyOrder(schema.optJSONArray("order"))
                ?: copyOrder(schema.optJSONObject("order")) ?: return
        if (target.isEmpty()) return
        copyProvider().put("order", target.toList().sortedBy { it.second }.map { it.first })
    }

    private fun copyOrder(orderArr: JSONArray?): Map<String, Int>? {
        if (orderArr == null || orderArr.length() == 0 || orderArr[0] is String) return null
        val ret = mutableMapOf<String, Int>()
        (0 until orderArr.length()).forEach { i ->
            orderArr.optJSONArray(i)?.also { arrEntry ->
                val offset = i * 1000
                (0 until arrEntry.length()).forEach { j ->
                    ret[arrEntry.getString(j)] = offset + j
                }
            }
            orderArr.optJSONObject(i)?.also { objEntry ->
                objEntry.keySet().forEach { key -> ret[key] = objEntry.getInt(key) }
            }
        }
        return ret
    }

    private fun copyOrder(orderObj: JSONObject?): Map<String, Int>? =
            orderObj?.keySet()?.associate { key -> key to orderObj.getInt(key) }

    private fun convertOrderInProperties(subPart: JSONObject, copyTarget: () -> JSONObject) {
        val properties = subPart.optJSONObject("properties") ?: return
        properties.keySet()?.forEach {
            convertOrder(properties.getJSONObject(it)) {
                copyTarget().getJSONObject("properties").getJSONObject(it)
            }
        }
    }

    private fun convertOrderInItems(subPart: JSONObject, copyTarget: () -> JSONObject) {
        val arrItems = subPart.optJSONObject("items")
        if (arrItems != null) {
            convertOrder(arrItems) {
                copyTarget().getJSONObject("items")
            }
        }
        subPart.optJSONArray("items")?.forEachIndexed { index, obj ->
            convertOrder(obj as JSONObject) {
                copyTarget().getJSONArray("items").getJSONObject(index)
            }
        }
    }

    private fun merge(target: JSONObject, source: JSONObject, keyBlackList: Set<String> = emptySet()): JSONObject =
            (source.keySet() - keyBlackList).fold(target) { acc, key ->
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