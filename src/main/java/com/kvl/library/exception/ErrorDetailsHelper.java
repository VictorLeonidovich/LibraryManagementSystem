package com.kvl.library.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Централизованная фабрика и помощник по обработке ошибок приложения.
 * Автоматически сопоставляет технические исключения со строгими бизнес-контрактами.
 */
@Slf4j
public class ErrorDetailsHelper {

    /**
     * Полноценная фабрика: определяет бизнес-код ошибки на основе класса исключения.
     */
    public static ApiErrorCode resolveErrorCode(Exception ex) {
        if (ex instanceof EntityNotFoundException ||
                ex instanceof NoHandlerFoundException ||
                ex instanceof NoResourceFoundException) {
            return ApiErrorCode.ENTITY_NOT_FOUND;
        }
        if (ex instanceof IllegalArgumentException) {
            return ApiErrorCode.BUSINESS_RULE_VIOLATION;
        }
        // По умолчанию для всех непредвиденных исключений
        return ApiErrorCode.INTERNAL_SERVER_ERROR;
    }

    /**
     * Унифицированное логирование на основе определенного Enum-кода.
     */
    public static void logException(Exception ex, HttpServletRequest request, ApiErrorCode errorCode) {
        String path = request != null ? request.getRequestURI() : "UNKNOWN_PATH";
        HttpStatus status = errorCode.getHttpStatus();

        if (status.is5xxServerError()) {
            log.error("CRITICAL SERVER ERROR occurred at [{}], Code {}: ", path, errorCode.getValue(), ex);
        } else {
            log.warn("Exception handled at [{}], Code {}: {}", path, errorCode.getValue(), ex.getMessage());
        }
    }
}