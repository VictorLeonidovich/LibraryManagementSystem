package com.kvl.library.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Единый строгий контракт и формат ошибок для REST API и Web UI.
 * Связывает воедино код, HTTP-статус, сообщения для API и страницы интерфейса.
 */
@Getter
@RequiredArgsConstructor
public enum ApiErrorCode {

    ENTITY_NOT_FOUND("ENTITY_NOT_FOUND", HttpStatus.NOT_FOUND, "Resource not found exception", "Запрашиваемая страница не найдена."),
    VALIDATION_FAILED("VALIDATION_FAILED", HttpStatus.BAD_REQUEST, "Validation failed for request fields", "Ошибка валидации данных формы."),
    AUTHENTICATION_FAILED("AUTHENTICATION_FAILED", HttpStatus.UNAUTHORIZED, "Неверное имя пользователя или пароль", "Неверное имя пользователя или пароль."),
    ACCESS_DENIED("ACCESS_DENIED", HttpStatus.FORBIDDEN, "You have no permissions to perform this operation.", "У вас нет прав для выполнения этой операции."),
    BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", HttpStatus.BAD_REQUEST, "Business rule violation", "Нарушение бизнес-правила приложения."),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal error occurred on the server.", "Произошла внутренняя ошибка сервера. Пожалуйста, попробуйте позже.");

    private final String value;
    private final HttpStatus httpStatus;
    private final String defaultMessage;
    private final String uiMessage; // Сообщение, которое отобразит Thymeleaf на HTML-странице
}