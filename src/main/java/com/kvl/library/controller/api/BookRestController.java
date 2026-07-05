package com.kvl.library.controller.api;

import com.kvl.library.dto.BookRequestDTO;
import com.kvl.library.dto.BookResponseDTO;
import com.kvl.library.entity.Book;
import com.kvl.library.mapper.BookMapper;
import com.kvl.library.service.core.AuthorService;
import com.kvl.library.service.book.BookService;
import com.kvl.library.service.core.CategoryService;
import com.kvl.library.service.core.PublisherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "Книги", description = "Управление каталогом книг (Доступ: USER/ADMIN)")
public class BookRestController {

    private final BookService bookService;
    private final AuthorService authorService;
    private final CategoryService categoryService;
    private final PublisherService publisherService;
    private final BookMapper bookMapper;

    @GetMapping("/popular-isbns")
    @Operation(summary = "Получить список ISBN популярных книг",
            description = "Возвращает список из 10 популярных ISBN. Данные рассчитываются на лету и сортируются в Redis с помощью структуры Sorted Set для разгрузки БД.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список популярных ISBN успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public ResponseEntity<List<String>> getPopularBookIsbns() {
        return ResponseEntity.ok(bookService.findPopularBookIsbns());
    }

    // GET /api/v1/books?keyword=Война&page=0&size=10&sort=name,asc
    @GetMapping
    @Operation(summary = "Получить список книг с пагинацией и фильтрацией",
            description = "Позволяет искать книги по ключевому слову в названии или описании. Доступно всем аутентифицированным пользователям.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список книг успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public ResponseEntity<Page<BookResponseDTO>> getAllBooks(
            @Parameter(description = "Ключевое слово для поиска (по названию/описанию)", example = "Война")
            @RequestParam(required = false) String keyword,
            @ParameterObject @PageableDefault(size = 10, sort = "name") Pageable pageable) {

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
    @Operation(summary = "Получить книгу по её ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Книга найдена"),
            @ApiResponse(responseCode = "404", description = "Книга с таким ID отсутствует в базе данных")
    })
    public ResponseEntity<BookResponseDTO> getBookById(
            @Parameter(description = "Идентификатор книги", example = "1")
            @PathVariable Long id) {
        Book book = bookService.findBookById(id);
        return ResponseEntity.ok(bookMapper.toResponseDTO(book));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать новую книгу", description = "Доступно только пользователям с ролью ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Книга успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные (ошибка валидации)"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (отсутствует роль ADMIN)")
    })
    public ResponseEntity<BookResponseDTO> createBook(@Valid @RequestBody BookRequestDTO requestDTO) {
        Book book = bookMapper.toEntity(requestDTO);
        mapRelations(requestDTO, book);
        bookService.createBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookMapper.toResponseDTO(book));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить существующую книгу по ID", description = "Полное обновление полей книги и её связей. Доступно только ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Книга успешно обновлена"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Книга не найдена")
    })
    public ResponseEntity<BookResponseDTO> updateBook(
            @Parameter(description = "Идентификатор обновляемой книги", example = "1")
            @PathVariable Long id,
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
    @Operation(summary = "Удалить книгу по ID", description = "Доступно только пользователям с ролью ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Книга успешно удалена (нет содержимого)"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Книга не найдена")
    })
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "Идентификатор удаляемой книги", example = "1")
            @PathVariable Long id) {
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