package com.kvl.library.repository;

import com.kvl.library.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.test.database.replace=NONE",
        "spring.profiles.active=test"
})
@DisplayName("CategoryRepository Data JPA Tests")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    private Category sciFi;
    private Category drama;

    @BeforeEach
    void setUp() {
        // Очищаем базу данных перед каждым тестом
        categoryRepository.deleteAll();

        // Подготавливаем тестовые данные (используем конструктор из сущности Category)
        sciFi = new Category("Научная фантастика");
        drama = new Category("Драма");

        // Сохраняем категории в базу данных
        categoryRepository.save(sciFi);
        categoryRepository.save(drama);
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase should return page of filtered categories regardless of case")
    void findByNameContainingIgnoreCase_WhenMatchExists_ShouldReturnFilteredPage() {
        String searchName = "фАнТаСтИкА"; // Смешанный регистр для проверки LOWER() в запросе
        Pageable pageable = PageRequest.of(0, 10);

        Page<Category> resultPage = categoryRepository.findByNameContainingIgnoreCase(searchName, pageable);

        assertThat(resultPage).isNotEmpty();
        assertThat(resultPage.getTotalElements()).isEqualTo(1);
        assertThat(resultPage.getContent().get(0).getName()).isEqualTo("Научная фантастика");
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase should return empty page when no categories match query")
    void findByNameContainingIgnoreCase_WhenNoMatch_ShouldReturnEmptyPage() {
        String searchName = "Детектив";
        Pageable pageable = PageRequest.of(0, 10);

        Page<Category> resultPage = categoryRepository.findByNameContainingIgnoreCase(searchName, pageable);

        assertThat(resultPage).isEmpty();
        assertThat(resultPage.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase should handle pagination correctly")
    void findByNameContainingIgnoreCase_WithPagination_ShouldReturnCorrectSlice() {
        String commonChar = "а"; // Буква 'а' есть и в "Научная фантастика", и в "Драма"
        Pageable pageable = PageRequest.of(0, 1); // Запрашиваем 1-ю страницу с размером в 1 элемент

        Page<Category> resultPage = categoryRepository.findByNameContainingIgnoreCase(commonChar, pageable);

        assertThat(resultPage.getContent()).hasSize(1);
        assertThat(resultPage.getTotalElements()).isEqualTo(2); // Всего в базе 2 подходящие категории
        assertThat(resultPage.getTotalPages()).isEqualTo(2);    // Всего должно получиться 2 страницы
    }

    @Test
    @DisplayName("findById should return category when it exists in database")
    void findById_WhenExists_ShouldReturnCategory() {
        Optional<Category> foundCategory = categoryRepository.findById(sciFi.getId());

        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getName()).isEqualTo("Научная фантастика");
    }

    @Test
    @DisplayName("save should persist new category with generated ID")
    void save_ShouldPersistCategory() {
        Category newCategory = new Category("Фэнтези");

        Category savedCategory = categoryRepository.save(newCategory);

        assertThat(savedCategory.getId()).isNotNull();
        assertThat(categoryRepository.findById(savedCategory.getId())).isPresent();
    }

    @Test
    @DisplayName("deleteById should remove category from database")
    void deleteById_WhenExists_ShouldRemoveCategory() {
        categoryRepository.deleteById(sciFi.getId());
        Optional<Category> deletedCategory = categoryRepository.findById(sciFi.getId());

        assertThat(deletedCategory).isEmpty();
    }
}