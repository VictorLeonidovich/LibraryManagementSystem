package com.kvl.library.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Данные для входа в систему")
public class UserLoginDto {

    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Schema(description = "Уникальное имя пользователя", example = "admin")
    private String username;

    @NotBlank(message = "Пароль не может быть пустым")
    @Schema(description = "Пароль пользователя", example = "admin123")
    private String password;
}