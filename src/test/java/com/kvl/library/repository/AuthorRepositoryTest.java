package com.kvl.library.repository;

import com.kvl.library.entity.Author;
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
@DisplayName("AuthorRepository Data JPA Tests")
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    private Author pushkin;
    private Author tolstoy;

    @BeforeEach
    void setUp() {
        // Очищаем базу данных перед каждым тестом
        authorRepository.deleteAll();

        // Подготавливаем тестовые данные (используем конструктор из сущности Author)
        pushkin = new Author("Александр Пушкин", "Великий русский поэт, драматург и прозаик.");
        tolstoy = new Author("Лев Толстой", "Великий русский писатель, мыслитель и классик мировой литературы.");

        // Сохраняем авторов в базу данных
        authorRepository.save(pushkin);
        authorRepository.save(tolstoy);
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase should return page of filtered authors regardless of case")
    void findByNameContainingIgnoreCase_WhenMatchExists_ShouldReturnFilteredPage() {
        String searchName = "тоЛсТоЙ"; // Смешанный регистр для проверки LOWER() в запросе
        Pageable pageable = PageRequest.of(0, 10);

        Page<Author> resultPage = authorRepository.findByNameContainingIgnoreCase(searchName, pageable);

        assertThat(resultPage).isNotEmpty();
        assertThat(resultPage.getTotalElements()).isEqualTo(1);
        assertThat(resultPage.getContent().get(0).getName()).isEqualTo("Лев Толстой");
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase should return empty page when no authors match query")
    void findByNameContainingIgnoreCase_WhenNoMatch_ShouldReturnEmptyPage() {
        String searchName = "Гоголь";
        Pageable pageable = PageRequest.of(0, 10);

        Page<Author> resultPage = authorRepository.findByNameContainingIgnoreCase(searchName, pageable);

        assertThat(resultPage).isEmpty();
        assertThat(resultPage.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase should handle pagination correctly")
    void findByNameContainingIgnoreCase_WithPagination_ShouldReturnCorrectSlice() {
        String commonChar = "л"; // Буква 'л' есть в именах "Александр" и "Лев"
        Pageable pageable = PageRequest.of(0, 1); // Запрашиваем 1-ю страницу с размером в 1 элемент

        Page<Author> resultPage = authorRepository.findByNameContainingIgnoreCase(commonChar, pageable);

        assertThat(resultPage.getContent()).hasSize(1);
        assertThat(resultPage.getTotalElements()).isEqualTo(2); // Всего в базе 2 подходящих автора
        assertThat(resultPage.getTotalPages()).isEqualTo(2);    // Всего должно получиться 2 страницы
    }

    @Test
    @DisplayName("findById should return author when it exists in database")
    void findById_WhenExists_ShouldReturnAuthor() {
        Optional<Author> foundAuthor = authorRepository.findById(pushkin.getId());

        assertThat(foundAuthor).isPresent();
        assertThat(foundAuthor.get().getName()).isEqualTo("Александр Пушкин");
    }

    @Test
    @DisplayName("save should persist new author with generated ID")
    void save_ShouldPersistAuthor() {
        Author newAuthor = new Author("Антон Чехов", "Выдающийся русский писатель, классик мировой литературы.");

        Author savedAuthor = authorRepository.save(newAuthor);

        assertThat(savedAuthor.getId()).isNotNull();
        assertThat(authorRepository.findById(savedAuthor.getId())).isPresent();
    }

    @Test
    @DisplayName("deleteById should remove author from database")
    void deleteById_WhenExists_ShouldRemoveAuthor() {
        authorRepository.deleteById(pushkin.getId());
        Optional<Author> deletedAuthor = authorRepository.findById(pushkin.getId());

        assertThat(deletedAuthor).isEmpty();
    }
}