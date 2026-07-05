package com.kvl.library.service.core.impl;

import com.kvl.library.entity.Author;
import com.kvl.library.exception.EntityNotFoundException;
import com.kvl.library.repository.AuthorRepository;
import com.kvl.library.service.core.AuthorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Реализация сервиса управления авторами.
 * <p>
 * Класс инкапсулирует бизнес-логику работы с авторами и координирует транзакции
 * при взаимодействии с уровнем доступа к данным ({@link AuthorRepository}).
 * Все методы чтения помечены как {@code readOnly = true} для оптимизации работы с СУБД.
 */
@Slf4j
@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param authorRepository репозиторий для управления сущностями авторов
     */
    public AuthorServiceImpl(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Author> findAllAuthors() {
        log.info("Fetching all authors as a list for relational select dropdowns");
        return authorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Author> findAllAuthors(Pageable pageable) {
        log.info("Fetching a page of authors from the database");
        return authorRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Author> searchAuthorsByName(String name, Pageable pageable) {
        log.info("Searching authors by name containing '{}'", name);
        return authorRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Author findAuthorById(final Long id) {
        Author author = findById(id);
        log.info("Fetched author '{}' by id '{}' from the database", author, id);
        return author;
    }

    /**
     * Внутренний приватный вспомогательный метод для поиска автора по ID.
     * Выбрасывает бизнес-исключение в случае отсутствия записи в базе данных.
     *
     * @param id уникальный идентификатор автора
     * @return найденная сущность автора
     * @throws EntityNotFoundException если автор с таким ID не существует
     */
    private Author findById(final Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Author with ID " + id + " was not found"));
    }

    @Override
    @Transactional
    public void createAuthor(final Author author) {
        log.info("Saving author '{}' to the database", author);
        authorRepository.save(author);
    }

    @Override
    @Transactional
    public void updateAuthor(final Author author) {
        log.info("Updating author '{}' in the database", author);
        // Предохранитель: предотвращает генерацию INSERT для несуществующего ID
        if (!authorRepository.existsById(author.getId())) {
            throw new EntityNotFoundException("Author with ID " + author.getId() + " was not found");
        }
        authorRepository.save(author);
    }

    @Override
    @Transactional
    public void deleteAuthor(final Long id) {
        // Сначала выполняем поиск, чтобы сгенерировать 404/Exception, если записи нет
        final Author author = findById(id);
        log.info("Deleting author '{}' by id '{}' from the database", author, id);
        authorRepository.deleteById(author.getId());
    }
}