package com.kvl.library.service;

import com.kvl.library.entity.Book;
import com.kvl.library.exception.EntityNotFoundException;
import com.kvl.library.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // Для классического MVC (Thymeleaf UI)
    @Transactional(readOnly = true)
    public List<Book> findAllBooks() {
        log.info("Fetching all books from the database");
        return bookRepository.findAll();
    }

    // 1. Пагинация общего списка для REST API
    @Transactional(readOnly = true)
    public Page<Book> findAllBooks(Pageable pageable) {
        log.info("Fetching a page of books from the database");
        return bookRepository.findAll(pageable);
    }

    // 2. Поиск по кастомному запросу (название/ISBN) с пагинацией для REST API
    @Transactional(readOnly = true)
    public Page<Book> searchBooks(String keyword, Pageable pageable) {
        log.info("Searching books by keyword '{}'", keyword);
        return bookRepository.searchByNameOrIsbn(keyword, pageable);
    }

    @Transactional(readOnly = true)
    public Book findBookById(final Long id) {
        Book book = findById(id);
        log.info("Fetched book '{}' by id '{}' from the database", book, id);
        return book;
    }

    private Book findById(final Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with ID " + id + " was not found"));
    }

    @Transactional
    public void createBook(final Book book) {
        log.info("Saving book '{}' to the database", book);
        bookRepository.save(book);
    }

    @Transactional
    public void updateBook(final Book book) {
        log.info("Updating book '{}' in the database", book);
        // Предохранитель от нежелательного INSERT при обновлении формы
        if (!bookRepository.existsById(book.getId())) {
            throw new EntityNotFoundException("Book with ID " + book.getId() + " was not found");
        }
        bookRepository.save(book);
    }

    @Transactional
    public void deleteBook(final Long id) {
        final Book book = findById(id);
        log.info("Deleting book '{}' by id '{}' from the database", book, id);
        bookRepository.deleteById(book.getId());
    }
}