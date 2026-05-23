package com.dbwp031.authservice.common.exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(
        exception: BusinessException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val errorCode = exception.errorCode
        return ResponseEntity.status(errorCode.status).body(
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = errorCode.status.value(),
                code = errorCode.name,
                message = exception.message ?: errorCode.message,
                path = request.requestURI,
            ),
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        exception: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val message = exception.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "Invalid request."
        return validationResponse(message, request)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(
        exception: ConstraintViolationException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> = validationResponse(exception.message ?: "Invalid request.", request)

    @ExceptionHandler(Exception::class)
    fun handleException(
        exception: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                code = "INTERNAL_SERVER_ERROR",
                message = "Unexpected server error.",
                path = request.requestURI,
            ),
        )

    private fun validationResponse(
        message: String,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity.badRequest().body(
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                code = "VALIDATION_ERROR",
                message = message,
                path = request.requestURI,
            ),
        )
}

data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val code: String,
    val message: String,
    val path: String,
)
