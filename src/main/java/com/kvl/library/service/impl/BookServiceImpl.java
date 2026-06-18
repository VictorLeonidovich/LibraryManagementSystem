package com.kvl.library.service.impl;

import com.kvl.library.config.CacheConfig;
import com.kvl.library.entity.Book;
import com.kvl.library.exception.EntityNotFoundException;
import com.kvl.library.repository.BookRepository;
import com.kvl.library.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final StringRedisTemplate redisTemplate;

    private static final String POPULAR_BOOKS_KEY = "library:books:views";

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param bookRepository репозиторий для управления сущностями книг
     * @param redisTemplate шаблон для взаимодействия со структурой данных Redis
     */
    public BookServiceImpl(BookRepository bookRepository,
                           @Autowired(required = false) StringRedisTemplate redisTemplate) {
        this.bookRepository = bookRepository;
        this.redisTemplate = redisTemplate;
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

        // Инкрементируем счетчик просмотров книги в Sorted Set базы данных Redis
        try {
            if (redisTemplate != null && book.getIsbn() != null && !book.getIsbn().isBlank()) {
                redisTemplate.opsForZSet().incrementScore(POPULAR_BOOKS_KEY, book.getIsbn(), 1);
            }
        } catch (Exception e) {
            log.warn("Failed to increment book view in Redis: {}", e.getMessage());
        }

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
        log.info("Updating book '{}' in the database", book);
        // Предохранитель от создания дубликатов при отправке некорректных веб-форм
        if (!bookRepository.existsById(book.getId())) {
            throw new EntityNotFoundException("Book with ID " + book.getId() + " was not found");
        }
        bookRepository.save(book);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.POPULAR_ISBNS_CACHE, allEntries = true)
    public void deleteBook(final Long id) {
        final Book book = findById(id);
        log.info("Deleting book '{}' by id '{}' from the database", book, id);

        // Удаляем книгу из общего чарта популярности в Redis при её физическом удалении
        try {
            if (redisTemplate != null && book.getIsbn() != null && !book.getIsbn().isBlank()) {
                redisTemplate.opsForZSet().remove(POPULAR_BOOKS_KEY, book.getIsbn());
            }
        } catch (Exception e) {
            log.warn("Failed to remove book from Redis sorted set: {}", e.getMessage());
        }

        bookRepository.deleteById(book.getId());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.POPULAR_ISBNS_CACHE, key = "'top10'")
    public List<String> findPopularBookIsbns() {
        log.info("--> [Промах кэша] Сборка Топ-10 из базы данных или Sorted Set Redis");
        try {
            if (redisTemplate != null) {
                java.util.Set<String> typedTupleSet = redisTemplate.opsForZSet().reverseRange(POPULAR_BOOKS_KEY, 0, 9);
                if (typedTupleSet != null && !typedTupleSet.isEmpty()) {
                    return typedTupleSet.stream().toList();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch from Redis sorted set, falling back to DB: {}", e.getMessage());
        }

        return bookRepository.findTop10Isbns(PageRequest.of(0, 10));
    }
}