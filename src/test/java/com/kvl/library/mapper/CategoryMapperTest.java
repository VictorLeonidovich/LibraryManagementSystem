package com.kvl.library.mapper;

import com.kvl.library.dto.CategoryRequestDTO;
import com.kvl.library.dto.CategoryResponseDTO;
import com.kvl.library.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CategoryMapper Unit Tests")
class CategoryMapperTest {

    // Вручную инициализируем сгенерированную MapStruct реализацию
    private final CategoryMapper categoryMapper = new CategoryMapperImpl();

    @Test
    @DisplayName("toResponseDTO should map Category entity to CategoryResponseDTO correctly")
    void toResponseDTO_ShouldMapEntityToResponseDTO() {
        // Arrange
        Category category = new Category("Научная фантастика");
        category.setId(7L);

        // Act
        CategoryResponseDTO responseDTO = categoryMapper.toResponseDTO(category);

        // Assert
        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getId()).isEqualTo(category.getId());
        assertThat(responseDTO.getName()).isEqualTo(category.getName());
    }

    @Test
    @DisplayName("toEntity should map CategoryRequestDTO to Category entity and ignore id and books")
    void toEntity_ShouldMapRequestDTOToEntityAndIgnoreFields() {
        CategoryRequestDTO requestDTO = new CategoryRequestDTO();
        requestDTO.setName("Детектив");

        Category category = categoryMapper.toEntity(requestDTO);

        assertThat(category).isNotNull();
        assertThat(category.getId()).isNull();          // Гарантируем работу @Mapping(target = "id", ignore = true)
        assertThat(category.getBooks()).isEmpty();       // Гарантируем работу @Mapping(target = "books", ignore = true)
        assertThat(category.getName()).isEqualTo(requestDTO.getName());
    }

    @Test
    @DisplayName("updateEntityFromDto should modify existing entity name and preserve its id and books relationship")
    void updateEntityFromDto_ShouldModifyExistingEntityAndPreserveId() {
        Category existingCategory = new Category("Старое название фэнтези");
        existingCategory.setId(99L);

        CategoryRequestDTO updateDTO = new CategoryRequestDTO();
        updateDTO.setName("Классическое фэнтези");

        categoryMapper.updateEntityFromDto(updateDTO, existingCategory);

        assertThat(existingCategory.getId()).isEqualTo(99L); // ID не изменился
        assertThat(existingCategory.getName()).isEqualTo("Классическое фэнтези"); // Название обновилось
        assertThat(existingCategory.getBooks()).isEmpty(); // Связи не затёрлись в null
    }

    @Test
    @DisplayName("toResponseDTO should return null when source entity is null")
    void toResponseDTO_WhenSourceIsNull_ShouldReturnNull() {
        assertThat(categoryMapper.toResponseDTO(null)).isNull();
    }

    @Test
    @DisplayName("toEntity should return null when source DTO is null")
    void toEntity_WhenSourceIsNull_ShouldReturnNull() {
        assertThat(categoryMapper.toEntity(null)).isNull();
    }
}