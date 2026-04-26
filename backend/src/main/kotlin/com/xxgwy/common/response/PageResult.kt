package com.xxgwy.common.response

data class PageResult<T>(
    val list: List<T>,
    val total: Long,
    val page: Int,
    val size: Int
) {
    companion object {
        @JvmStatic
        fun <T> of(list: List<T>, total: Long, page: Int, size: Int): PageResult<T> =
            PageResult(list, total, page, size)
    }
}
