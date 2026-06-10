package com.kvl.library.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Данные созданного пользователя")
public class UserResponseDto {

    @Schema(description = "Идентификатор пользователя в БД", example = "1")
    private Long id;

    @Schema(description = "Имя зарегистрированного пользователя", example = "new_user")
    private String username;

    @Schema(description = "Назначенная роль", example = "ROLE_USER")
    private String role;

    @Schema(description = "Статус выполнения операции", example = "Пользователь успешно зарегистрирован!")
    private String message;
}