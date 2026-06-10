package com.kvl.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "DTO запроса для создания или обновления книги")
public class BookRequestDTO {

    @NotEmpty(message = "Имя не должно быть пустым")
    @Size(min = 2, max = 50, message = "Имя должно быть длиной от 2 до 50 символов")
    @Schema(description = "Название книги", example = "Война и мир")
    private String name;

    @NotEmpty(message = "ISBN не должно быть пустым")
    @Size(min = 2, max = 50, message = "ISBN должно быть длиной от 2 до 50 символов")
    @Schema(description = "Международный стандартный книжный номер", example = "978-5-17-201211-1")
    private String isbn;

    @NotEmpty(message = "Описание не должно быть пустым")
    @Size(min = 2, max = 250, message = "Описание должно быть длиной от 2 до 250 символов")
    @Schema(description = "Краткое описание содержания", example = "Эпический роман Льва Николаевича Толстого")
    private String description;

    // Receive collections of relationship relational IDs from the client
    @Schema(description = "ID авторов книги", example = "[1, 2]")
    private Set<Long> authorIds;

    @Schema(description = "ID категорий книги", example = "[3]")
    private Set<Long> categoryIds;

    @Schema(description = "ID издателей книги", example = "[1]")
    private Set<Long> publisherIds;
}