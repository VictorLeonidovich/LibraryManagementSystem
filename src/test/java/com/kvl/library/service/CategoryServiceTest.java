package com.kvl.library.service;

import com.kvl.library.entity.Category;
import com.kvl.library.exception.EntityNotFoundException;
import com.kvl.library.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Unit Tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private final Long categoryId = 1L;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(categoryId);
        testCategory.setName("Фантастика");
    }

    @Test
    @DisplayName("findAllCategories should return list of categories")
    void findAllCategories_ShouldReturnList() {
        List<Category> expectedCategories = Collections.singletonList(testCategory);
        when(categoryRepository.findAll()).thenReturn(expectedCategories);

        List<Category> actualCategories = categoryService.findAllCategories();

        assertThat(actualCategories).isNotEmpty().hasSize(1).contains(testCategory);
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findCategoryById should return category when found")
    void findCategoryById_WhenExists_ShouldReturnCategory() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));

        Category actualCategory = categoryService.findCategoryById(categoryId);

        assertThat(actualCategory).isNotNull();
        assertThat(actualCategory.getId()).isEqualTo(categoryId);
        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    @DisplayName("findCategoryById should throw EntityNotFoundException when not found")
    void findCategoryById_WhenNotFound_ShouldThrowException() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findCategoryById(categoryId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Category with ID " + categoryId + " was not found");

        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    @DisplayName("createCategory should save category successfully")
    void createCategory_ShouldSaveCategory() {
        when(categoryRepository.save(testCategory)).thenReturn(testCategory);

        categoryService.createCategory(testCategory);

        verify(categoryRepository, times(1)).save(testCategory);
    }

    @Test
    @DisplayName("updateCategory should save updated category successfully")
    void updateCategory_ShouldSaveCategory() {
        when(categoryRepository.save(testCategory)).thenReturn(testCategory);

        categoryService.updateCategory(testCategory);

        verify(categoryRepository, times(1)).save(testCategory);
    }

    @Test
    @DisplayName("deleteCategory should delete category when exists")
    void deleteCategory_WhenExists_ShouldDelete() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        doNothing().when(categoryRepository).deleteById(categoryId);

        categoryService.deleteCategory(categoryId);

        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).deleteById(categoryId);
    }

    @Test
    @DisplayName("deleteCategory should throw EntityNotFoundException and not delete when not found")
    void deleteCategory_WhenNotFound_ShouldThrowException() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Category with ID " + categoryId + " was not found");

        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, never()).deleteById(anyLong());
    }
}