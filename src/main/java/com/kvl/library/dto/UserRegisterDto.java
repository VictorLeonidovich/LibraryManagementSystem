package com.kvl.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDto {

    @NotBlank(message = "Имя пользователя не должно быть пустым")
    @Size(min = 4, max = 20, message = "Имя пользователя должно быть от 4 до 20 символов")
    private String username;

    @NotBlank(message = "Пароль не должен быть пустым")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    private String password;
}