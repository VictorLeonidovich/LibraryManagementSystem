package com.kvl.library.service;

import com.kvl.library.entity.Book;
import com.kvl.library.exception.EntityNotFoundException;
import com.kvl.library.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@Deprecated
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findAllBooks() {
        log.info("Fetching all books from the database");
        return bookRepository.findAll();
    }

    public Book findBookById(final Long id) {
        Book book = findById(id);
        log.info("Fetched book '{}' by id '{}' from the database", book, id);
        return book;
    }

    private Book findById(final Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with ID " + id + " was not found"));
    }

    public void createBook(final Book book) {
        log.info("Saving book '{}' to the database", book);
        bookRepository.save(book);
    }

    public void updateBook(final Book book) {
        log.info("Updating book '{}' in the database", book);
        bookRepository.save(book);
    }

    public void deleteBook(final Long id) {
        final Book book = findById(id);
        log.info("Deleting book '{}' by id '{}' from the database", book, id);
        bookRepository.deleteById(book.getId());
    }
}
