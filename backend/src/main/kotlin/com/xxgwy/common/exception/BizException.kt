package com.xxgwy.common.exception

class BizException(val httpStatus: Int, message: String) : RuntimeException(message) {

    companion object {
        fun conflict(message: String): BizException = BizException(409, message)
        fun unauthorized(message: String): BizException = BizException(401, message)
        fun notFound(message: String): BizException = BizException(404, message)
        fun badRequest(message: String): BizException = BizException(400, message)
        fun forbidden(message: String): BizException = BizException(403, message)
    }
}
