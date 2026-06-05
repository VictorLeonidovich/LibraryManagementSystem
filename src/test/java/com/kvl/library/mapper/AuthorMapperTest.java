package com.kvl.library.mapper;

import com.kvl.library.dto.AuthorRequestDTO;
import com.kvl.library.dto.AuthorResponseDTO;
import com.kvl.library.entity.Author;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthorMapper Unit Tests")
class AuthorMapperTest {

    // Создаем экземпляр сгенерированной реализации маппера вручную
    private final AuthorMapper authorMapper = new AuthorMapperImpl();

    @Test
    @DisplayName("toResponseDTO should map Author entity to AuthorResponseDTO correctly")
    void toResponseDTO_ShouldMapEntityToResponseDTO() {
        Author author = new Author("Лев Толстой", "Великий русский писатель.");
        author.setId(42L);

        AuthorResponseDTO responseDTO = authorMapper.toResponseDTO(author);

        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getId()).isEqualTo(author.getId());
        assertThat(responseDTO.getName()).isEqualTo(author.getName());
        assertThat(responseDTO.getDescription()).isEqualTo(author.getDescription());
    }

    @Test
    @DisplayName("toEntity should map AuthorRequestDTO to Author entity and ignore id and books")
    void toEntity_ShouldMapRequestDTOToEntityAndIgnoreFields() {
        AuthorRequestDTO requestDTO = new AuthorRequestDTO();
        requestDTO.setName("Александр Пушкин");
        requestDTO.setDescription("Великий русский поэт.");

        Author author = authorMapper.toEntity(requestDTO);

        assertThat(author).isNotNull();
        assertThat(author.getId()).isNull();          // Проверяем @Mapping(target = "id", ignore = true)
        assertThat(author.getBooks()).isEmpty();       // Проверяем @Mapping(target = "books", ignore = true)
        assertThat(author.getName()).isEqualTo(requestDTO.getName());
        assertThat(author.getDescription()).isEqualTo(requestDTO.getDescription());
    }

    @Test
    @DisplayName("updateEntityFromDto should modify existing entity fields and preserve its id")
    void updateEntityFromDto_ShouldModifyExistingEntityAndPreserveId() {
        Author existingAuthor = new Author("Старое Имя", "Старое описание");
        existingAuthor.setId(100L);

        AuthorRequestDTO updateDTO = new AuthorRequestDTO();
        updateDTO.setName("Новое Имя");
        updateDTO.setDescription("Новое описание");

        authorMapper.updateEntityFromDto(updateDTO, existingAuthor);

        assertThat(existingAuthor.getId()).isEqualTo(100L); // ID должен остаться прежним
        assertThat(existingAuthor.getName()).isEqualTo("Новое Имя");
        assertThat(existingAuthor.getDescription()).isEqualTo("Новое описание");
    }

    @Test
    @DisplayName("toResponseDTO should return null when source entity is null")
    void toResponseDTO_WhenSourceIsNull_ShouldReturnNull() {
        assertThat(authorMapper.toResponseDTO(null)).isNull();
    }

    @Test
    @DisplayName("toEntity should return null when source DTO is null")
    void toEntity_WhenSourceIsNull_ShouldReturnNull() {
        assertThat(authorMapper.toEntity(null)).isNull();
    }
}