package com.xxgwy.common.response

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class R<T>(
    val code: Int = 200,
    val message: String = "ok",
    val data: T? = null
) {
    companion object {
        @JvmStatic
        fun <T> ok(): R<T> = R()

        @JvmStatic
        fun <T> ok(data: T): R<T> = R(data = data)

        @JvmStatic
        fun <T> failed(message: String): R<T> = R(500, message)

        @JvmStatic
        fun <T> failed(code: Int, message: String): R<T> = R(code, message)
    }
}
