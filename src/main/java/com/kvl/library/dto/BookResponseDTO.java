package com.kvl.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "DTO ответа со сведениями о книге")
public class BookResponseDTO {

    @Schema(description = "Идентификатор книги в БД", example = "105")
    private Long id;

    @Schema(description = "Название книги", example = "Война и мир")
    private String name;

    @Schema(description = "ISBN книги", example = "978-5-17-201211-1")
    private String isbn;

    @Schema(description = "Описание книги", example = "Эпический роман Льва Николаевича Толстого")
    private String description;

    // Flattened structural references to avoid deep recursive loops
    @Schema(description = "Список авторов книги")
    private Set<AuthorResponseDTO> authors;

    @Schema(description = "Список категорий книги")
    private Set<CategoryResponseDTO> categories;

    @Schema(description = "Список издателей книги")
    private Set<PublisherResponseDTO> publishers;
}