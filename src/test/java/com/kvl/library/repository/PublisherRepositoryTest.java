package com.kvl.library.repository;

import com.kvl.library.entity.Publisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PublisherRepository Integration Tests with Testcontainers (PostgreSQL)")
class PublisherRepositoryTest extends BaseContainersTest {

    @Autowired
    private PublisherRepository publisherRepository;

    private Publisher eksmo;
    private Publisher prosveshchenie;

    @BeforeEach
    void setUp() {
        // Очищаем базу данных перед каждым тестом
        publisherRepository.deleteAll();

        // Подготавливаем тестовые данные (используем конструктор из сущности Publisher)
        eksmo = new Publisher("Эксмо");
        prosveshchenie = new Publisher("Просвещение");

        // Сохраняем издателей в базу данных
        publisherRepository.save(eksmo);
        publisherRepository.save(prosveshchenie);
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase should return page of filtered publishers regardless of case")
    void findByNameContainingIgnoreCase_WhenMatchExists_ShouldReturnFilteredPage() {
        String searchName = "эКсМо"; // Смешанный регистр для проверки LOWER() в запросе на PostgreSQL
        Pageable pageable = PageRequest.of(0, 10);

        Page<Publisher> resultPage = publisherRepository.findByNameContainingIgnoreCase(searchName, pageable);

        assertThat(resultPage).isNotEmpty();
        assertThat(resultPage.getTotalElements()).isEqualTo(1);
        assertThat(resultPage.getContent().get(0).getName()).isEqualTo("Эксмо");
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase should return empty page when no publishers match query")
    void findByNameContainingIgnoreCase_WhenNoMatch_ShouldReturnEmptyPage() {
        String searchName = "АСТ";
        Pageable pageable = PageRequest.of(0, 10);

        Page<Publisher> resultPage = publisherRepository.findByNameContainingIgnoreCase(searchName, pageable);

        assertThat(resultPage).isEmpty();
        assertThat(resultPage.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase should handle pagination correctly")
    void findByNameContainingIgnoreCase_WithPagination_ShouldReturnCorrectSlice() {
        String commonChar = "о"; // Буква 'о' есть и в "Эксмо", и в "Просвещение"
        Pageable pageable = PageRequest.of(0, 1); // Запрашиваем 1-ю страницу с размером в 1 элемент

        Page<Publisher> resultPage = publisherRepository.findByNameContainingIgnoreCase(commonChar, pageable);

        assertThat(resultPage.getContent()).hasSize(1);
        assertThat(resultPage.getTotalElements()).isEqualTo(2); // Всего в базе 2 подходящих издателя
        assertThat(resultPage.getTotalPages()).isEqualTo(2);    // Всего должно получиться 2 страницы
    }

    @Test
    @DisplayName("findById should return publisher when it exists in database")
    void findById_WhenExists_ShouldReturnPublisher() {
        Optional<Publisher> foundPublisher = publisherRepository.findById(eksmo.getId());

        assertThat(foundPublisher).isPresent();
        assertThat(foundPublisher.get().getName()).isEqualTo("Эксмо");
    }

    @Test
    @DisplayName("save should persist new publisher with generated ID")
    void save_ShouldPersistPublisher() {
        Publisher newPublisher = new Publisher("Питер");

        Publisher savedPublisher = publisherRepository.save(newPublisher);

        assertThat(savedPublisher.getId()).isNotNull();
        assertThat(publisherRepository.findById(savedPublisher.getId())).isPresent();
    }

    @Test
    @DisplayName("deleteById should remove publisher from database")
    void deleteById_WhenExists_ShouldRemovePublisher() {
        publisherRepository.deleteById(eksmo.getId());
        Optional<Publisher> deletedPublisher = publisherRepository.findById(eksmo.getId());

        assertThat(deletedPublisher).isEmpty();
    }
}