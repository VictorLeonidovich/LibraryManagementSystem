package com.kvl.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "DTO ответа со сведениями о категории")
public class CategoryResponseDTO {

    @Schema(description = "Идентификатор категории в БД", example = "5")
    private Long id;

    @Schema(description = "Название категории литературы", example = "Фантастика")
    private String name;
}