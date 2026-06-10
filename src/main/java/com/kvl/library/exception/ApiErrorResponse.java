package com.kvl.library.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@Schema(description = "Унифицированная структура ответа при возникновении ошибок API")
public class ApiErrorResponse {

    @Schema(description = "Дата и время возникновения ошибки", example = "2026-06-10T22:45:30")
    private final LocalDateTime timestamp;

    @Schema(description = "HTTP статус-код ошибки", example = "400")
    private final int status;

    @Schema(description = "Текстовое описание HTTP статуса", example = "Bad Request")
    private final String error;

    @Schema(description = "Сообщение с деталями ошибки", example = "Validation failed")
    private final String message;

    @Schema(description = "Относительный путь эндпоинта, на котором произошла ошибка", example = "/api/v1/books")
    private final String path;

    @Schema(description = "Список ошибок валидации полей (заполняется только при коде 400 для некорректных DTO)",
            example = "{\"isbn\": \"ISBN не должно быть пустым\", \"name\": \"Имя должно быть длиной от 2 до 50 символов\"}")
    private final Map<String, String> validationErrors;
}