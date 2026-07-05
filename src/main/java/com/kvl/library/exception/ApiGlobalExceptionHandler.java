package com.kvl.library.exception;

import com.kvl.library.dto.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений для всех REST API контроллеров.
 */
@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class ApiGlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        log.error("Resource not found exception at [{}]: {}", request.getRequestURI(), ex.getMessage());
        // Передаем ex.getMessage(), если хотим сохранить динамическую деталь (например, ID), иначе — дефолтное из Enum
        return buildResponse(ApiErrorCode.ENTITY_NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("Validation failed for parameters on path: {}", request.getRequestURI());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            } else {
                errors.put(error.getObjectName(), error.getDefaultMessage());
            }
        });

        return buildResponse(ApiErrorCode.VALIDATION_FAILED, ApiErrorCode.VALIDATION_FAILED.getDefaultMessage(), request, errors);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(org.springframework.security.core.AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication failed at [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(ApiErrorCode.AUTHENTICATION_FAILED, ApiErrorCode.AUTHENTICATION_FAILED.getDefaultMessage(), request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.error("Access denied at [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(ApiErrorCode.ACCESS_DENIED, ApiErrorCode.ACCESS_DENIED.getDefaultMessage(), request, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessExceptions(IllegalArgumentException ex, HttpServletRequest request) {
        log.error("Business rule violation at [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(ApiErrorCode.BUSINESS_RULE_VIOLATION, ex.getMessage(), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllUncaughtExceptions(Exception ex, HttpServletRequest request) {
        log.error("An unexpected critical error occurred at endpoint {}: ", request.getRequestURI(), ex);
        return buildResponse(ApiErrorCode.INTERNAL_SERVER_ERROR, ApiErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage(), request, null);
    }

    /**
     * Унифицированный фабричный метод построения ответа на основе строгого Enum-контракта.
     */
    private ResponseEntity<ApiErrorResponse> buildResponse(
            ApiErrorCode errorCode,
            String message,
            HttpServletRequest request,
            Map<String, String> validationErrors) {

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getHttpStatus().value())
                .error(errorCode.getHttpStatus().getReasonPhrase())
                .errorCode(errorCode)
                .message(message)
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }
}