package com.kvl.library.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Глобальный обработчик исключений для веб-интерфейса Thymeleaf.
 * <p>
 * Применяется ко всем классам, помеченным аннотацией {@link Controller} (и исключает REST-контроллеры).
 * Выполняет перенаправление на специализированные HTML-страницы ошибок интерфейса.
 */
@ControllerAdvice(annotations = Controller.class)
public class GlobalExceptionHandler {

    @Value("${app.errors.show-details:false}")
    private boolean showDetails;

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public String handleNotFoundError(Exception ex, HttpServletRequest request, Model model) {
        // 1. Фабрика определяет тип ошибки
        ApiErrorCode errorCode = ErrorDetailsHelper.resolveErrorCode(ex);

        // 2. Фабрика логирует в стандартном формате
        ErrorDetailsHelper.logException(ex, request, errorCode);

        // 3. Достаем текст сообщения напрямую из контракта фабрики
        model.addAttribute("errorMessage", errorCode.getUiMessage());
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    public String handleGlobalException(Exception ex, HttpServletRequest request, Model model) {
        // 1. Фабрика определяет тип ошибки
        ApiErrorCode errorCode = ErrorDetailsHelper.resolveErrorCode(ex);

        // 2. Фабрика логирует в стандартном формате
        ErrorDetailsHelper.logException(ex, request, errorCode);

        // 3. Достаем текст сообщения напрямую из контракта фабрики
        model.addAttribute("errorMessage", errorCode.getUiMessage());

        if (showDetails) {
            model.addAttribute("technicalDetails", ex.getMessage()); // Не рекомендуется показывать пользователям в продакшене
        } else {
            model.addAttribute("technicalDetails", null);
        }
        return "error/500";
    }
}