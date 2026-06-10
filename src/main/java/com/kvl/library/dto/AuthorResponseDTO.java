package com.kvl.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "DTO ответа со сведениями об авторе")
public class AuthorResponseDTO {

    @Schema(description = "Идентификатор автора в БД", example = "12")
    private Long id;

    @Schema(description = "Полное имя автора", example = "Лев Николаевич Толстой")
    private String name;

    @Schema(description = "Краткие сведения об авторе", example = "Один из наиболее известных писателей и мыслителей мира.")
    private String description;
}