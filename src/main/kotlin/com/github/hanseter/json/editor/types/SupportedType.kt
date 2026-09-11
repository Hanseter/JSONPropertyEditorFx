package com.github.hanseter.json.editor.types

import org.json.JSONArray
import org.json.JSONObject

sealed interface SupportedType<T> {
    fun coerce(obj: Any): T
    sealed interface ComplexType<T> : SupportedType<T> {
        object ObjectType : ComplexType<JSONObject?>{
            override fun coerce(obj: Any): JSONObject? = obj as? JSONObject
        }
        object ArrayType : ComplexType<JSONArray?>{
            override fun coerce(obj: Any): JSONArray? = obj as? JSONArray
        }
        object TupleType : ComplexType<JSONArray?>{
            override fun coerce(obj: Any): JSONArray? = obj as? JSONArray
        }
        object OneOfType : ComplexType<Any?>{
            override fun coerce(obj: Any): Any? = obj
        }
    }

    sealed interface SimpleType<T> : SupportedType<T> {
        object EnumType : SimpleType<String?> {
            override fun coerce(obj: Any): String? = StringType.coerce(obj)
        }
        object EnumSetType : SimpleType<JSONArray?> {
            override fun coerce(obj: Any): JSONArray? = ComplexType.ArrayType.coerce(obj)
        }
        object StringType : SimpleType<String?> {
            override fun coerce(obj: Any): String? = obj as? String ?: obj.toString()
        }
        object IdReferenceType : SimpleType<String?> {
            override fun coerce(obj: Any): String? = StringType.coerce(obj)
        }
        object ResourceType : SimpleType<String?> {
            override fun coerce(obj: Any): String? = StringType.coerce(obj)
        }
        object ColorType : SimpleType<String?> {
            override fun coerce(obj: Any): String? = StringType.coerce(obj)
        }
        object IntType : SimpleType<Int?> {
            override fun coerce(obj: Any): Int? = (obj as? Number)?.toInt()
        }
        object DoubleType : SimpleType<Double?> {
            override fun coerce(obj: Any): Double? =  (obj as? Number)?.toDouble()
        }
        object BooleanType : SimpleType<Boolean?> {
            override fun coerce(obj: Any): Boolean? = obj as? Boolean
        }

        object UnsupportedType : SimpleType<Any?> {
            override fun coerce(obj: Any): Any? = obj
        }
    }

}