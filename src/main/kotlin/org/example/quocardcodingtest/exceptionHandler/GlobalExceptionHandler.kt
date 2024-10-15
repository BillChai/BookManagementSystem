package org.example.quocardcodingtest.exceptionHandler

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.example.quocardcodingtest.exception.DatabaseException
import org.example.quocardcodingtest.exception.InternalServerErrorException
import org.example.quocardcodingtest.exception.ResourceNotFoundException
import org.example.quocardcodingtest.exception.UpdateBadRequestException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class GlobalExceptionHandler {

    /**
     * validation exception
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<String> {
        val errors = ex.bindingResult.allErrors.joinToString(", ") { it.defaultMessage ?: "無効なフィールド" }
        return ResponseEntity(errors, ex.statusCode)
    }

    /**
     * resource not found exception
     */
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException): ResponseEntity<String> {
        val body = mapOf("message" to ex.message).toString()
        return ResponseEntity(body, HttpStatus.NOT_FOUND)
    }

    /**
     * resource update bad request
     */
    @ExceptionHandler(UpdateBadRequestException::class)
    fun handleUpdateBadRequestException(ex: ResourceNotFoundException): ResponseEntity<String> {
        val body = mapOf("message" to ex.message).toString()
        return ResponseEntity(body, HttpStatus.BAD_REQUEST)
    }

    /**
     * Internal Server Error exception
     */
    @ExceptionHandler(InternalServerErrorException::class)
    fun handleUpdateBadRequestException(ex: InternalServerErrorException): ResponseEntity<String> {
        val body = mapOf("message" to ex.message).toString()
        return ResponseEntity(body, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    /**
     * DB Internal Server Error exception
     */
    @ExceptionHandler(DatabaseException::class)
    fun handleDatabaseException(ex: DatabaseException): ResponseEntity<String> {
        val body = mapOf("message" to ex.message).toString()
        return ResponseEntity(body, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}