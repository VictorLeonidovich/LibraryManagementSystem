package com.kvl.library.controller;

import com.kvl.library.dto.BookRequestDTO;
import com.kvl.library.dto.BookResponseDTO;
import com.kvl.library.entity.Book;
import com.kvl.library.mapper.BookMapper;
import com.kvl.library.service.AuthorService;
import com.kvl.library.service.BookService;
import com.kvl.library.service.CategoryService;
import com.kvl.library.service.PublisherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookRestController {

    private final BookService bookService;
    private final AuthorService authorService;
    private final CategoryService categoryService;
    private final PublisherService publisherService;
    private final BookMapper bookMapper;

    // GET /api/v1/books?keyword=Война&page=0&size=10&sort=name,asc
    @GetMapping
    public ResponseEntity<Page<BookResponseDTO>> getAllBooks(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {

        Page<Book> booksPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            booksPage = bookService.searchBooks(keyword, pageable);
        } else {
            booksPage = bookService.findAllBooks(pageable);
        }

        Page<BookResponseDTO> responsePage = booksPage.map(bookMapper::toResponseDTO);
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> getBookById(@PathVariable Long id) {
        Book book = bookService.findBookById(id);
        return ResponseEntity.ok(bookMapper.toResponseDTO(book));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookResponseDTO> createBook(@Valid @RequestBody BookRequestDTO requestDTO) {
        Book book = bookMapper.toEntity(requestDTO);
        mapRelations(requestDTO, book);
        bookService.createBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookMapper.toResponseDTO(book));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookResponseDTO> updateBook(@PathVariable Long id,
                                                      @Valid @RequestBody BookRequestDTO requestDTO) {
        Book existingBook = bookService.findBookById(id);
        bookMapper.updateEntityFromDto(requestDTO, existingBook);

        // Clear prior relational entries to prevent caching duplicate records
        existingBook.setAuthors(new HashSet<>());
        existingBook.setCategories(new HashSet<>());
        existingBook.setPublishers(new HashSet<>());

        mapRelations(requestDTO, existingBook);
        bookService.updateBook(existingBook);
        return ResponseEntity.ok(bookMapper.toResponseDTO(existingBook));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    // Helper handler to bind relational sets safely
    private void mapRelations(BookRequestDTO dto, Book book) {
        if (dto.getAuthorIds() != null) {
            dto.getAuthorIds().forEach(authorId -> book.addAuthor(authorService.findAuthorById(authorId)));
        }
        if (dto.getCategoryIds() != null) {
            dto.getCategoryIds().forEach(categoryId -> book.addCategory(categoryService.findCategoryById(categoryId)));
        }
        if (dto.getPublisherIds() != null) {
            dto.getPublisherIds().forEach(publisherId -> book.addPublisher(publisherService.findPublisherById(publisherId)));
        }
    }
}