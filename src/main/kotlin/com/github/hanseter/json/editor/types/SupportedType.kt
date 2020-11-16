package com.github.hanseter.json.editor.types

import org.json.JSONArray
import org.json.JSONObject

sealed class SupportedType<T> {
    sealed class ComplexType<T> : SupportedType<T>() {
        object ObjectType : ComplexType<JSONObject?>()
        object ArrayType : ComplexType<JSONArray?>()
        object TupleType : ComplexType<JSONArray?>()
        object OneOfType : ComplexType<Any?>()
    }

    sealed class SimpleType<T> : SupportedType<T>() {
        object EnumType : SimpleType<String?>()
        object StringType : SimpleType<String?>()
        object IdReferenceType : SimpleType<String?>()
        object ColorType : SimpleType<String?>()
        object IntType : SimpleType<Int?>()
        object DoubleType : SimpleType<Double?>()
        object BooleanType : SimpleType<Boolean?>()

        object UnsupportedType : SimpleType<Any?>()
    }
}