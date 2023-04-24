package com.github.hanseter.json.editor

import com.github.hanseter.json.editor.schemaExtensions.ColorFormat
import com.github.hanseter.json.editor.schemaExtensions.IdReferenceFormat
import com.github.hanseter.json.editor.schemaExtensions.LocalTimeFormat
import org.everit.json.schema.Schema
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.UncheckedIOException
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths

object SchemaNormalizer {

    private val logger = LoggerFactory.getLogger(SchemaNormalizer::class.java)


    fun parseSchema(
        schema: JSONObject,
        resolutionScope: URI?,
        readOnly: Boolean
    ): Schema = SchemaLoader.builder()
        .useDefaults(true)
        .draftV7Support()
        .addFormatValidator(ColorFormat.Validator)
        .addFormatValidator(IdReferenceFormat.Validator())
        .addFormatValidator(LocalTimeFormat.Validator)
        .schemaJson(normalizeSchema(schema, resolutionScope))
        .build().load().readOnly(readOnly).build()

    /**
     * Normalizes a schema. I.e. it resolves all `$refs` and inlines all compositions.
     */
    fun normalize(
        schema: JSONObject,
        resolveFunc: SchemaResolver
    ): JSONObject =
        removeArtifacts(convertOrder(inlineCompositions(resolveRefsInternal(schema, resolveFunc))))

    /**
     * Normalizes a schema. I.e. it resolves all `$refs` and inlines all compositions.
     */
    fun normalize(
        schema: JSONObject,
        otherSchemas: Map<String, JSONObject>
    ): JSONObject =
        normalize(schema, MapBasedSchemaResolver(otherSchemas))

    /**
     * Normalizes a schema. I.e. it resolves all `$refs` and inlines all compositions.
     * `$refs` will be resolved relatively to [resolutionScope].
     */
    fun normalize(schema: JSONObject, resolutionScope: URI) =
        normalize(schema, UriBasedSchemaResolver(resolutionScope))

    /**
     * Normalizes a schema. I.e. it resolves all `$refs` and inlines all compositions.
     * `$refs` will be resolved relatively to [resolutionScope].
     */
    @Deprecated(message = "Use `normalize` instead")
    fun normalizeSchema(schema: JSONObject, resolutionScope: URI?) =
        if (resolutionScope == null) normalize(schema)
        else normalize(schema, resolutionScope)

    /**
     * Normalizes a schema. I.e. it resolves all `$refs` and inlines all compositions.
     * With this overload, only local and fully qualified `$refs` can be resolved.
     */
    fun normalize(schema: JSONObject) =
        normalize(schema, UriBasedSchemaResolver(null))

    /**
     * Resolves $refs in a schema.
     *
     * @param schema the schema to resolve the references in
     * @param resolutionScope the URI to resolve $refs to other files from
     * @param completeSchema the schema to resolve internal $refs relative to (usually the root schema), or `null` to resolve them relative to `schema`
     * @return a schema where the $refs have been resolved
     */
    fun resolveRefs(
        schema: JSONObject,
        resolutionScope: URI?,
        completeSchema: JSONObject? = null
    ): JSONObject = resolveRefs(schema, UriBasedSchemaResolver(resolutionScope), completeSchema)

    /**
     * Resolves $refs in a schema.
     *
     * @param schema the schema to resolve the references in
     * @param resolutionScope the URI to resolve $refs to other files from
     * @param completeSchema the schema to resolve internal $refs relative to (usually the root schema), or `null` to resolve them relative to `schema`
     * @return a schema where the $refs have been resolved
     */
    fun resolveRefs(
        schema: JSONObject,
        otherSchemas: Map<String, JSONObject>,
        completeSchema: JSONObject? = null
    ): JSONObject =
        resolveRefs(schema, MapBasedSchemaResolver(otherSchemas), completeSchema)

    /**
     * Resolves $refs in a schema.
     *
     * @param schema the schema to resolve the references in
     * @param resolutionScope a callback to resolve a $ref
     * @param completeSchema the schema to resolve internal $refs relative to (usually the root schema), or `null` to resolve them relative to `schema`
     * @return a schema where the $refs have been resolved
     */
    fun resolveRefs(
        schema: JSONObject,
        resolutionScope: SchemaResolver,
        completeSchema: JSONObject? = null
    ): JSONObject =
        removeArtifacts(RefInliner().resolveRefs(schema, resolutionScope, completeSchema))

    private fun resolveRefsInternal(
        schema: JSONObject,
        resolutionScope: SchemaResolver,
        completeSchema: JSONObject? = null
    ): JSONObject = RefInliner().resolveRefs(schema, resolutionScope, completeSchema)

    fun deepCopy(toCopy: JSONObject): JSONObject =
        toCopy.keySet().fold(JSONObject()) { acc, it ->
            acc.put(it, deepCopy(toCopy.get(it)))
        }

    fun deepCopy(toCopy: JSONArray): JSONArray = toCopy.fold(JSONArray()) { acc, it ->
        acc.put(deepCopy(it))
    }

    fun <T> deepCopy(toCopy: T): T = when (toCopy) {
        is JSONObject -> deepCopy(toCopy) as T
        is JSONArray -> deepCopy(toCopy) as T
        else -> toCopy
    }

    private fun removeArtifacts(obj: JSONObject): JSONObject {
        val iter = obj.keys()
        while (iter.hasNext()) {
            val key = iter.next()
            if (key == "${"$"}ref") {
                iter.remove()
                continue
            }
            when (val value = obj.get(key)) {
                is JSONObject -> removeArtifacts(value)
                is JSONArray -> removeArtifacts(value)
            }
        }
        return obj
    }

    private fun removeArtifacts(arr: JSONArray): JSONArray {
        (0 until arr.length()).forEach { i ->
            when (val value = arr.get(i)) {
                is JSONObject -> removeArtifacts(value)
                is JSONArray -> removeArtifacts(value)
            }
        }
        return arr
    }

    /**
     * Inlines all all-ofs in a schema. I.e. the all-ofs will be removed and merged into one big object.
     */
    fun inlineCompositions(schema: JSONObject): JSONObject {
        val copy = lazy { deepCopy(schema) }
        inlineCompositions(schema, copy)
        return if (copy.isInitialized()) copy.value else schema
    }

    private tailrec fun inlineCompositions(
        subPart: JSONObject,
        copyTarget: Lazy<JSONObject>
    ) {
        inlineInValidationOnly(subPart, copyTarget)

        if (inlineInProperties(subPart, copyTarget)) return
        if (inlineInAdditionalProperties(subPart, copyTarget)) return
        if (inlineInOneOf(subPart, copyTarget)) return
        if (inlineInItems(subPart, copyTarget)) return

        val allOf = subPart.optJSONArray("allOf") ?: return

        copyTarget.value.apply {
            remove("allOf")
        }

        val order = lazy { JSONArray() }
        val notCopiedKeys = copyTarget.value.keySet() + "order" + "properties"
        getDistinctEntriesInAllOf(allOf).forEach { propObj ->
            propObj.optJSONObject("properties")?.let { newSubProps ->
                val oldProps = copyTarget.value.optJSONObject("properties") ?: JSONObject()
                merge(oldProps, newSubProps)
                copyTarget.value.put("properties", oldProps)
            }
            propObj.opt("order")?.let {
                if (it is JSONObject || it is JSONArray) {
                    order.value.put(it)
                }
            }

            merge(copyTarget.value, propObj, notCopiedKeys)
        }
        if (order.isInitialized()) {
            copyTarget.value.put("order", order.value)
        }

        inlineCompositions(copyTarget.value, copyTarget)
    }

    private fun inlineInProperties(subPart: JSONObject, copyTarget: Lazy<JSONObject>): Boolean {
        val properties = subPart.optJSONObject("properties")
        if (properties != null) {
            properties.keySet().forEach {
                inlineCompositions(properties.getJSONObject(it), lazy {
                    copyTarget.value.getJSONObject("properties").getJSONObject(it)
                })
            }
            return true
        }
        return false
    }

    private fun inlineInAdditionalProperties(
        subPart: JSONObject,
        copyTarget: Lazy<JSONObject>
    ): Boolean {
        val properties = subPart.optJSONObject("additionalProperties")
        if (properties != null) {
            inlineCompositions(properties, lazy {
                copyTarget.value.getJSONObject("additionalProperties")
            })
            return true
        }
        return false
    }

    private fun inlineInOneOf(subPart: JSONObject, copyTarget: Lazy<JSONObject>): Boolean {
        val composition = subPart.optJSONArray("oneOf")
        if (composition != null) {
            composition.forEachIndexed { index, obj ->
                if (obj is JSONObject) {
                    inlineCompositions(obj, lazy {
                        copyTarget.value.getJSONArray("oneOf").getJSONObject(index)
                    })
                }
            }
            return true
        }
        return false
    }

    private fun inlineInItems(subPart: JSONObject, copyTarget: Lazy<JSONObject>): Boolean {
        val arrItems = subPart.optJSONObject("items")
        if (arrItems != null) {
            inlineCompositions(arrItems, lazy {
                copyTarget.value.getJSONObject("items")
            })
            return true
        }
        val tupleItems = subPart.optJSONArray("items")
        if (tupleItems != null) {
            tupleItems.forEachIndexed { index, obj ->
                inlineCompositions(obj as JSONObject, lazy {
                    copyTarget.value.getJSONArray("items").getJSONObject(index)
                })
            }
            return true
        }
        return false
    }

    private fun inlineInValidationOnly(subPart: JSONObject, copyTarget: Lazy<JSONObject>) {
        inlineCompositionsForKey("not", subPart, copyTarget)
        inlineCompositionsForKey("if", subPart, copyTarget)
        inlineCompositionsForKey("then", subPart, copyTarget)
        inlineCompositionsForKey("else", subPart, copyTarget)
    }

    private fun inlineCompositionsForKey(
        key: String,
        subPart: JSONObject,
        copyTarget: Lazy<JSONObject>
    ) {
        subPart.optJSONObject(key)?.let { ifSchema ->
            inlineCompositions(ifSchema, lazy {
                copyTarget.value.getJSONObject(key)
            })
        }
    }

    private fun getDistinctEntriesInAllOf(allOf: JSONArray): List<JSONObject> {
        var i = -1
        return getAllEntriesInAllOf(allOf).distinctBy { it.optInt("${"$"}ref", i--) }
    }

    /**
     * Gets all sub-schemas inside an `allOf` array.
     * If the array contains items other than objects, they are filtered out.
     * If the array contains nested `allOf`s, they are flattened.
     */
    private fun getAllEntriesInAllOf(allOf: JSONArray): List<JSONObject> {
        return (0 until allOf.length()).flatMap { i ->
            val allOfEntry = allOf.get(i)
            if (allOfEntry is JSONObject) {
                val nestedAllOff = allOfEntry.optJSONArray("allOf")
                if (nestedAllOff != null) {
                    if ((allOfEntry.keySet() - "allOf").isNotEmpty()) {
                        logger.warn(
                            "Encountered additional content in `allOf` during schema normalization. It will be discarded: {}",
                            allOfEntry.toString()
                        )
                    }

                    getAllEntriesInAllOf(nestedAllOff)
                } else {
                    listOf(allOfEntry)
                }
            } else {
                emptyList()
            }
        }
    }

    fun convertOrder(schema: JSONObject): JSONObject {
        val copy = lazy { deepCopy(schema) }
        convertOrder(schema, copy)
        return if (copy.isInitialized()) copy.value else schema
    }

    private fun convertOrder(schema: JSONObject, copyProvider: Lazy<JSONObject>) {
        convertOrderInProperties(schema, copyProvider)
        convertOrderInItems(schema, copyProvider)
        val target = copyOrder(schema.optJSONArray("order"))
            ?: copyOrder(schema.optJSONObject("order")) ?: return
        if (target.isEmpty()) return
        copyProvider.value.put("order", target.toList().sortedBy { it.second }.map { it.first })
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

    private fun convertOrderInProperties(subPart: JSONObject, copyTarget: Lazy<JSONObject>) {
        val properties = subPart.optJSONObject("properties") ?: return
        properties.keySet()?.forEach {
            convertOrder(properties.getJSONObject(it), lazy {
                copyTarget.value.getJSONObject("properties").getJSONObject(it)
            })
        }
    }

    private fun convertOrderInItems(subPart: JSONObject, copyTarget: Lazy<JSONObject>) {
        val arrItems = subPart.optJSONObject("items")
        if (arrItems != null) {
            convertOrder(arrItems, lazy {
                copyTarget.value.getJSONObject("items")
            })
        }
        subPart.optJSONArray("items")?.forEachIndexed { index, obj ->
            convertOrder(obj as JSONObject, lazy {
                copyTarget.value.getJSONArray("items").getJSONObject(index)
            })
        }
    }

}

fun merge(
    target: JSONObject,
    source: JSONObject,
    keyBlackList: Set<String> = emptySet()
): JSONObject =
    (source.keySet() - keyBlackList).fold(target) { acc, key ->
        val old = acc.optJSONObject(key)
        val new = source.optJSONObject(key)
        if (old == null || new == null) {
            val oldArray = acc.optJSONArray(key)
            val newArray = source.optJSONArray(key)

            if (newArray != null) {
                acc.put(key, mergeArrays(oldArray, newArray))
            } else {
                acc.put(key, SchemaNormalizer.deepCopy(source.get(key)))
            }
        } else {
            val merged = merge(old, new)
            acc.put(key, merged)
        }
        acc
    }

fun mergeArrays(target: JSONArray?, source: JSONArray): JSONArray {
    if (target == null) return SchemaNormalizer.deepCopy(source)
    source.forEach {
        target.put(SchemaNormalizer.deepCopy(it))
    }
    return target
}

data class ResolvedSchema(
    val schema: JSONObject,
    val resolveRelatively: (String) -> ResolvedSchema,
    val pointerFragment: String?
)


fun interface SchemaResolver {
    fun resolveSchema(ref: String): ResolvedSchema
}

private class MapBasedSchemaResolver(val schemas: Map<String, JSONObject>) : SchemaResolver {
    private fun resolveInMap(
        uri: String,
        currentPath: Path
    ): ResolvedSchema {
        val normalized = currentPath.resolveSibling(uri).normalize().toString().replace('\\', '/')
        val index = normalized.lastIndexOf('#')
        val file = if (index == -1) normalized else normalized.substring(0, index)
        val pointer = if (index == -1) null else normalized.substring(index)

        return ResolvedSchema(
            schemas[file] ?: throw IllegalStateException("Cannot resolve schema $file"),
            { resolveInMap(it, Paths.get(file)) },
            pointer
        )
    }

    override fun resolveSchema(ref: String): ResolvedSchema = resolveInMap(ref, Paths.get("."))

}

private class UriBasedSchemaResolver(val baseUrl: URI?) : SchemaResolver {
    private val cache = HashMap<URI, JSONObject>()
    override fun resolveSchema(ref: String): ResolvedSchema = resolveRefFromUrl(ref, baseUrl)

    private fun resolveRefFromUrl(url: String, resolutionScope: URI?): ResolvedSchema {
        fun get(uri: URI): JSONObject {
            if (!uri.isAbsolute) {
                throw IllegalArgumentException("""URI is not absolute: $uri""")
            }
            return cache.getOrPut(uri) {
                val conn = uri.toURL().openConnection()
                val location = conn.getHeaderField("Location")
                location?.let { get(URI(it)) } ?: conn.getInputStream().use {
                    JSONObject(JSONTokener(it.reader(Charsets.UTF_8)))
                }
            }
        }
        if (resolutionScope != null) {
            try {
                val fullUri = resolveJarAware(resolutionScope, url)
                val jarAwareUri = resolveJarAware(fullUri, ".")
                return ResolvedSchema(
                    get(fullUri),
                    { resolveRefFromUrl(it, jarAwareUri) },
                    fullUri.fragment
                )
            } catch (e: IOException) {
                //ignore exception
            }
        }
        try {
            val fullUri = URI(url)
            val jarAwareUri = resolveJarAware(fullUri, ".")
            return ResolvedSchema(
                get(fullUri),
                { resolveRefFromUrl(it, jarAwareUri) },
                fullUri.fragment
            )
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }

    private fun resolveJarAware(resolutionScope: URI, other: String): URI {
        if ("jar" != resolutionScope.scheme) return resolutionScope.resolve(other)
        val str = resolutionScope.toString()
        val idx = str.indexOf('!')
        val jarPath = str.substring(0, idx + 1)
        val sourceEntry = str.substring(idx + 1)
        val targetEntry: String = URI.create(sourceEntry).resolve(other).toString()
        return URI.create(jarPath + targetEntry)
    }

}

private class RefInliner {

    private val cache = HashMap<JSONObject, JSONObject>()

    fun resolveRefs(
        schema: JSONObject,
        resolutionScope: SchemaResolver,
        completeSchema: JSONObject? = null
    ): JSONObject {
        return cache.getOrPut(schema) {
            val copy = lazy { SchemaNormalizer.deepCopy(schema) }
            resolveRefs(completeSchema ?: schema, schema, resolutionScope, copy)
            if (copy.isInitialized()) copy.value else schema
        }
    }

    private fun resolveRefs(
        schema: JSONObject,
        schemaPart: JSONObject,
        schemaResolver: SchemaResolver,
        copyTarget: Lazy<JSONObject>
    ) {
        if (resolveRefsInAllOf(schemaPart, schema, schemaResolver, copyTarget)) return
        if (resolveRefsInOneOf(schemaPart, schema, schemaResolver, copyTarget)) return
        if (resolveRefsInProperties(schemaPart, schema, schemaResolver, copyTarget)) return
        if (resolveRefsInAdditionalProperties(
                schemaPart,
                schema,
                schemaResolver,
                copyTarget
            )
        ) return
        if (resolveRefsInItems(schemaPart, schema, schemaResolver, copyTarget)) return

        val ref = schemaPart.optString("\$ref", null) ?: return

        val referredSchema = if (ref.first() == '#') {
            resolveRefs(
                resolveRefInDocument(schema, ref.drop(2), schemaResolver),
                schemaResolver, schema
            )
        } else {
            val resolvedSchema = schemaResolver.resolveSchema(ref)

            val fullObject = resolvedSchema.schema

            val resolvedFragment = if (!resolvedSchema.pointerFragment.isNullOrBlank()) {
                fullObject.optQuery(resolvedSchema.pointerFragment) as? JSONObject
                    ?: throw IllegalArgumentException("Target of pointer ${resolvedSchema.pointerFragment} is not an object")
            } else {
                fullObject
            }

            resolveRefs(
                resolvedFragment,
                resolvedSchema.resolveRelatively,
                fullObject
            )
        }

        val target = copyTarget.value
        target.put("${"$"}ref", System.identityHashCode(referredSchema))
        referredSchema.keySet().forEach {
            if (!target.has(it)) {
                target.put(it, SchemaNormalizer.deepCopy(referredSchema.get(it)))
            }
        }
    }

    private fun resolveRefsInProperties(
        schemaPart: JSONObject,
        schema: JSONObject,
        schemaResolver: SchemaResolver,
        copyTarget: Lazy<JSONObject>
    ): Boolean {
        val properties = schemaPart.optJSONObject("properties")
        if (properties != null) {
            properties.keySet().forEach { key ->
                resolveRefs(schema, properties.getJSONObject(key), schemaResolver, lazy {
                    copyTarget.value.getJSONObject("properties").getJSONObject(key)
                })
            }
            return true
        }
        return false
    }

    private fun resolveRefsInAdditionalProperties(
        schemaPart: JSONObject,
        schema: JSONObject,
        schemaResolver: SchemaResolver,
        copyTarget: Lazy<JSONObject>
    ): Boolean {
        val properties = schemaPart.optJSONObject("additionalProperties")
        if (properties != null) {
            resolveRefs(schema, properties, schemaResolver, lazy {
                copyTarget.value.getJSONObject("additionalProperties")
            })
            return true
        }
        return false
    }

    private fun resolveRefsInItems(
        schemaPart: JSONObject,
        schema: JSONObject,
        schemaResolver: SchemaResolver,
        copyTarget: Lazy<JSONObject>
    ): Boolean {
        val arrayItems = schemaPart.optJSONObject("items")
        if (arrayItems != null) {
            resolveRefs(schema, arrayItems, schemaResolver, lazy {
                copyTarget.value.getJSONObject("items")
            })
            return true
        }
        val tupleItems = schemaPart.optJSONArray("items")
        if (tupleItems != null) {
            tupleItems.forEachIndexed { index, obj ->
                resolveRefs(schema, obj as JSONObject, schemaResolver, lazy {
                    copyTarget.value.getJSONArray("items").getJSONObject(index)
                })
            }
            return true
        }
        return false
    }

    private fun resolveRefsInAllOf(
        schemaPart: JSONObject,
        schema: JSONObject,
        schemaResolver: SchemaResolver,
        copyTarget: Lazy<JSONObject>
    ): Boolean =
        resolveRefsInComposition(schemaPart, schema, schemaResolver, copyTarget, "allOf")

    private fun resolveRefsInOneOf(
        schemaPart: JSONObject,
        schema: JSONObject,
        schemaResolver: SchemaResolver,
        copyTarget: Lazy<JSONObject>
    ): Boolean =
        resolveRefsInComposition(schemaPart, schema, schemaResolver, copyTarget, "oneOf")

    private fun resolveRefsInComposition(
        schemaPart: JSONObject,
        schema: JSONObject,
        schemaResolver: SchemaResolver,
        copyTarget: Lazy<JSONObject>,
        compositionType: String
    ): Boolean {
        val composition = schemaPart.optJSONArray(compositionType)
        if (composition != null) {
            composition.forEachIndexed { index, obj ->
                if (obj is JSONObject) {
                    resolveRefs(schema, obj, schemaResolver, lazy {
                        copyTarget.value.getJSONArray(compositionType).getJSONObject(index)
                    })
                }
            }
            return true
        }
        return false
    }

    private fun resolveRefInDocument(
        schema: JSONObject,
        referred: String,
        schemaResolver: SchemaResolver
    ): JSONObject {
        val pointer = referred.split('/')
        val referredSchema = queryObject(schema, pointer)
        resolveRefs(referredSchema, schemaResolver, schema)
        return referredSchema
    }

    private fun queryObject(schema: JSONObject, pointer: List<String>): JSONObject =
        queryObjOrArray(schema, pointer) as JSONObject

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
}