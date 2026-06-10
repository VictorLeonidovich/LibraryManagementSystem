package com.kvl.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "DTO ответа со сведениями об издателе")
public class PublisherResponseDTO {

    @Schema(description = "Идентификатор издателя в БД", example = "3")
    private Long id;

    @Schema(description = "Наименование издательства", example = "Эксмо")
    private String name;
}