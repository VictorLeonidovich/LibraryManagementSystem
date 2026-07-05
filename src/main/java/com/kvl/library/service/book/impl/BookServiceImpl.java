package com.kvl.library.service.book.impl;

import com.kvl.library.config.CacheConfig;
import com.kvl.library.entity.Book;
import com.kvl.library.exception.EntityNotFoundException;
import com.kvl.library.repository.BookRepository;
import com.kvl.library.service.book.BookService;
import com.kvl.library.service.book.BookPopularityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final BookPopularityService bookPopularityService;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param bookRepository репозиторий для управления сущностями книг
     * @param bookPopularityService сервис для ведения метрик популярности книг
     */
    public BookServiceImpl(BookRepository bookRepository, BookPopularityService bookPopularityService) {
        this.bookRepository = bookRepository;
        this.bookPopularityService = bookPopularityService;
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

        // Делегируем инкремент просмотров специализированному сервису популярности
        bookPopularityService.incrementView(book.getIsbn());

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
    @CacheEvict(value = CacheConfig.POPULAR_ISBNS_CACHE, allEntries = true)
    public void createBook(final Book book) {
        log.info("Saving book '{}' to the database", book);
        bookRepository.save(book);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.POPULAR_ISBNS_CACHE, allEntries = true)
    public void updateBook(final Book book) {
        log.info("Updating book with ID '{}' in the database", book.getId());

        // 1. Находим существующую валидную запись из базы данных (наш сохраненный граф)
        // Если книга не найдена, findById сам выбросит правильный EntityNotFoundException
        Book existingBook = findById(book.getId());

        // 2. Модифицируем (мутируем) только разрешенные бизнес-поля
        existingBook.setName(book.getName());
        existingBook.setIsbn(book.getIsbn());
        existingBook.setDescription(book.getDescription());

        // 3. Синхронизируем связи в памяти через утилитарные методы (очищаем старые, прописываем новые)
        existingBook.getAuthors().clear();
        if (book.getAuthors() != null) {
            book.getAuthors().forEach(existingBook::addAuthor);
        }

        existingBook.getCategories().clear();
        if (book.getCategories() != null) {
            book.getCategories().forEach(existingBook::addCategory);
        }

        existingBook.getPublishers().clear();
        if (book.getPublishers() != null) {
            book.getPublishers().forEach(existingBook::addPublisher);
        }

        // 4. Сохраняем обновленный управляемый объект (Managed Entity)
        bookRepository.save(existingBook);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.POPULAR_ISBNS_CACHE, allEntries = true)
    public void deleteBook(final Long id) {
        final Book book = findById(id);
        log.info("Deleting book '{}' by id '{}' from the database", book, id);

        // Делегируем удаление книги из чарта популярности сервису популярности
        bookPopularityService.removeBook(book.getIsbn());

        bookRepository.deleteById(book.getId());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.POPULAR_ISBNS_CACHE, key = "'top10'")
    public List<String> findPopularBookIsbns() {
        log.info("--> [Промах кэша] Сборка Топ-10 из базы данных или Sorted Set Redis");

        // Запрашиваем топ-10 из сервиса популярности
        List<String> topIsbns = bookPopularityService.getTopBooks(10);
        if (!topIsbns.isEmpty()) {
            return topIsbns;
        }

        return bookRepository.findTop10Isbns(PageRequest.of(0, 10));
    }
}