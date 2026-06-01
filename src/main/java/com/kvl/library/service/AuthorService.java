package com.kvl.library.service;

import com.kvl.library.entity.Author;
import com.kvl.library.exception.EntityNotFoundException;
import com.kvl.library.repository.AuthorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthorService {
    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    // 1. Пагинация для общего списка (readOnly транзакция)
    @Transactional(readOnly = true)
    public Page<Author> findAllAuthors(Pageable pageable) {
        log.info("Fetching a page of authors from the database");
        return authorRepository.findAll(pageable);
    }

    // 2. Использование кастомного запроса с пагинацией
    @Transactional(readOnly = true)
    public Page<Author> searchAuthorsByName(String name, Pageable pageable) {
        log.info("Searching authors by name containing '{}'", name);
        return authorRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional(readOnly = true)
    public Author findAuthorById(final Long id) {
        Author author = findById(id);
        log.info("Fetched author '{}' by id '{}' from the database", author, id);
        return author;
    }

    private Author findById(final Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Author with ID " + id + " was not found"));
    }

    // Изменяющие методы требуют полноценную пишущую транзакцию
    @Transactional
    public void createAuthor(final Author author) {
        log.info("Saving author '{}' to the database", author);
        authorRepository.save(author);
    }

    @Transactional
    public void updateAuthor(final Author author) {
        log.info("Updating author '{}' in the database", author);
        // Проверяем существование перед обновлением, чтобы случайно не сделать INSERT
        if (!authorRepository.existsById(author.getId())) {
            throw new EntityNotFoundException("Author with ID " + author.getId() + " was not found");
        }
        authorRepository.save(author);
    }

    @Transactional
    public void deleteAuthor(final Long id) {
        final Author author = findById(id);
        log.info("Deleting author '{}' by id '{}' from the database", author, id);
        authorRepository.deleteById(author.getId());
    }
}
