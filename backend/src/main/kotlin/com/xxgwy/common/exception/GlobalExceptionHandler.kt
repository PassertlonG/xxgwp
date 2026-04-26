package com.xxgwy.common.exception

import com.xxgwy.common.response.R
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(BizException::class)
    fun handleBizException(e: BizException): ResponseEntity<R<Nothing>> {
        log.warn("BizException: {}", e.message)
        return ResponseEntity.status(e.httpStatus).body(R.failed(e.httpStatus, e.message ?: "Business error"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<R<Nothing>> {
        val errors = e.bindingResult.allErrors.joinToString("; ") {
            (it as? FieldError)?.let { "${it.field}: ${it.defaultMessage}" } ?: it.defaultMessage ?: it.code ?: ""
        }
        log.warn("Validation failed: {}", errors)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(R.failed(400, errors))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(e: Exception): ResponseEntity<R<Nothing>> {
        log.error("Unexpected error", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(R.failed(500, "Internal server error"))
    }
}
