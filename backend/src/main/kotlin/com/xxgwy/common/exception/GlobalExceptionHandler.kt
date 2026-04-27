package com.xxgwy.common.exception

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.xxgwy.common.response.R
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(BizException::class)
    fun handleBizException(e: BizException): ResponseEntity<R<Nothing>> {
        log.warn("BizException: {}", e.message)
        return ResponseEntity.status(e.httpStatus).body(R.failed(e.httpStatus, e.message ?: "Business error"))
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(e: AccessDeniedException): ResponseEntity<R<Nothing>> {
        log.warn("Access denied: {}", e.message)
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(R.failed(403, "权限不足"))
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthentication(e: AuthenticationException): ResponseEntity<R<Nothing>> {
        log.warn("Authentication failed: {}", e.message)
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(R.failed(403, "未登录或Token无效"))
    }

    // ====================== 请求参数相关 ======================

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<R<Nothing>> {
        val errors = e.bindingResult.allErrors.joinToString("; ") {
            (it as? FieldError)?.let { "${it.field}: ${it.defaultMessage}" } ?: it.defaultMessage ?: it.code ?: ""
        }
        log.warn("Validation failed: {}", errors)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(R.failed(400, errors))
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParam(e: MissingServletRequestParameterException): ResponseEntity<R<Nothing>> {
        log.warn("Missing request parameter: {}", e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(R.failed(400, "缺少必要参数: ${e.parameterName}"))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(e: MethodArgumentTypeMismatchException): ResponseEntity<R<Nothing>> {
        log.warn("Type mismatch for parameter '{}': {}", e.name, e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(R.failed(400, "参数格式错误: ${e.name}"))
    }

    // ====================== JSON 反序列化相关 ======================

    @ExceptionHandler(MissingKotlinParameterException::class)
    fun handleMissingKotlinParam(e: MissingKotlinParameterException): ResponseEntity<R<Nothing>> {
        val fieldName = e.path?.lastOrNull()?.fieldName ?: "unknown"
        log.warn("Missing required JSON field: {}", fieldName)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(R.failed(400, "缺少必填字段: $fieldName"))
    }

    @ExceptionHandler(InvalidFormatException::class)
    fun handleInvalidFormat(e: InvalidFormatException): ResponseEntity<R<Nothing>> {
        val fieldName = e.path?.lastOrNull()?.fieldName ?: "unknown"
        log.warn("Invalid format for field '{}': {}", fieldName, e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(R.failed(400, "字段格式错误: $fieldName"))
    }

    @ExceptionHandler(UnrecognizedPropertyException::class)
    fun handleUnrecognizedProperty(e: UnrecognizedPropertyException): ResponseEntity<R<Nothing>> {
        log.warn("Unrecognized JSON property: {}", e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(R.failed(400, "未知字段: ${e.propertyName}"))
    }

    @ExceptionHandler(JsonParseException::class)
    fun handleJsonParse(e: JsonParseException): ResponseEntity<R<Nothing>> {
        log.warn("Malformed JSON: {}", e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(R.failed(400, "请求体JSON格式错误"))
    }

    @ExceptionHandler(JsonMappingException::class)
    fun handleJsonMapping(e: JsonMappingException): ResponseEntity<R<Nothing>> {
        log.warn("JSON mapping failed: {}", e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(R.failed(400, "请求体数据格式不匹配"))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMessageNotReadable(e: HttpMessageNotReadableException): ResponseEntity<R<Nothing>> {
        val cause = e.cause
        when (cause) {
            is JsonParseException, is JsonMappingException -> {
                // these are handled by more specific handlers above,
                // but if they fall through, catch generically here
            }
        }
        log.warn("Request body not readable: {}", e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(R.failed(400, "请求体无法读取，请检查JSON格式"))
    }

    // ====================== 资源不存在 ======================

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResource(e: NoResourceFoundException): ResponseEntity<R<Nothing>> {
        log.warn("Resource not found: {}", e.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(R.failed(404, "接口不存在"))
    }

    // ====================== 兜底 ======================

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<R<Nothing>> {
        log.warn("Illegal argument: {}", e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(R.failed(400, e.message ?: "参数错误"))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(e: Exception): ResponseEntity<R<Nothing>> {
        log.error("Unexpected error", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(R.failed(500, "Internal server error"))
    }
}
