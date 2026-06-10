package com.kvl.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "DTO запроса для создания или обновления автора")
public class AuthorRequestDTO {

    @NotEmpty(message = "Имя не должно быть пустым")
    @Size(min = 2, max = 100, message = "Имя должно быть длиной от 2 до 100 символов")
    @Schema(description = "Полное имя / псевдоним автора", example = "Лев Николаевич Толстой")
    private String name;

    @NotEmpty(message = "Описание не должно быть пустым")
    @Size(min = 2, max = 250, message = "Описание должно быть длиной от 2 до 250 символов")
    @Schema(description = "Краткая биография или сведения об авторе", example = "Один из наиболее известных писателей и мыслителей мира.")
    private String description;
}