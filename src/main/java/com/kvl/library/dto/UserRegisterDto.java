package com.kvl.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Данные для регистрации нового пользователя")
public class UserRegisterDto {

    @NotBlank(message = "Имя пользователя не должно быть пустым")
    @Size(min = 4, max = 20, message = "Имя пользователя должно быть от 4 до 20 символов")
    @Schema(description = "Уникальное имя пользователя в системе", example = "new_user")
    private String username;

    @NotBlank(message = "Пароль не должен быть пустым")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    @Schema(description = "Пароль учетной записи", example = "password123")
    private String password;
}