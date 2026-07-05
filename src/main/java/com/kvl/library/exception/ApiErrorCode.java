package com.kvl.library.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Единый строгий контракт и формат ошибок REST API.
 * Связывает воедино машинно-читаемый код, HTTP-статус и стандартное сообщение.
 */
@Getter
@RequiredArgsConstructor
public enum ApiErrorCode {

    ENTITY_NOT_FOUND("ENTITY_NOT_FOUND", HttpStatus.NOT_FOUND, "Resource not found exception"),
    VALIDATION_FAILED("VALIDATION_FAILED", HttpStatus.BAD_REQUEST, "Validation failed for request fields"),
    AUTHENTICATION_FAILED("AUTHENTICATION_FAILED", HttpStatus.UNAUTHORIZED, "Неверное имя пользователя или пароль"),
    ACCESS_DENIED("ACCESS_DENIED", HttpStatus.FORBIDDEN, "You have no permissions to perform this operation."),
    BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", HttpStatus.BAD_REQUEST, "Business rule violation"),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal error occurred on the server.");

    private final String value;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
}