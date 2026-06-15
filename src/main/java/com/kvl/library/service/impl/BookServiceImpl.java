package com.kvl.library.service.impl;

import com.kvl.library.entity.Book;
import com.kvl.library.exception.EntityNotFoundException;
import com.kvl.library.repository.BookRepository;
import com.kvl.library.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Реализация сервиса управления книжным фондом.
 * <p>
 * Обеспечивает сквозное ведение транзакций и валидацию идентификаторов
 * перед операциями модификации данных. Взаимодействует с {@link BookRepository}.
 */
@Slf4j
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param bookRepository репозиторий для управления сущностями книг
     */
    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> findAllBooks() {
        log.info("Fetching all books from the database");
        return bookRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Book> findAllBooks(Pageable pageable) {
        log.info("Fetching a page of books from the database");
        return bookRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Book> searchBooks(String keyword, Pageable pageable) {
        log.info("Searching books by keyword '{}'", keyword);
        return bookRepository.searchByNameOrIsbn(keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Book findBookById(final Long id) {
        Book book = findById(id);
        log.info("Fetched book '{}' by id '{}' from the database", book, id);
        return book;
    }

    /**
     * Внутренний метод поиска книги с централизованной обработкой отсутствия записи.
     *
     * @param id идентификатор книги
     * @return сущность книги
     * @throws EntityNotFoundException если книга не найдена
     */
    private Book findById(final Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with ID " + id + " was not found"));
    }

    @Override
    @Transactional
    public void createBook(final Book book) {
        log.info("Saving book '{}' to the database", book);
        bookRepository.save(book);
    }

    @Override
    @Transactional
    public void updateBook(final Book book) {
        log.info("Updating book '{}' in the database", book);
        // Предохранитель от создания дубликатов при отправке некорректных веб-форм
        if (!bookRepository.existsById(book.getId())) {
            throw new EntityNotFoundException("Book with ID " + book.getId() + " was not found");
        }
        bookRepository.save(book);
    }

    @Override
    @Transactional
    public void deleteBook(final Long id) {
        final Book book = findById(id);
        log.info("Deleting book '{}' by id '{}' from the database", book, id);
        bookRepository.deleteById(book.getId());
    }
}