package com.kvl.library.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvl.library.controller.BaseWebContainersTest;
import com.kvl.library.dto.BookRequestDTO;
import com.kvl.library.entity.Author;
import com.kvl.library.entity.Book;
import com.kvl.library.entity.Category;
import com.kvl.library.entity.Publisher;
import com.kvl.library.exception.ApiErrorCode;
import com.kvl.library.repository.AuthorRepository;
import com.kvl.library.repository.BookRepository;
import com.kvl.library.repository.CategoryRepository;
import com.kvl.library.repository.PublisherRepository;
import com.kvl.library.security.JwtRequestFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("BookRestController Integration Tests")
class BookRestControllerContainersTest extends BaseWebContainersTest { // 1. Наследуемся от общего веб-класса

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 2. Внедряем реальные репозитории вместо заглушек-сервисов
    @Autowired private BookRepository bookRepository;
    @Autowired private AuthorRepository authorRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private PublisherRepository publisherRepository;

    // 3. Заглушки безопасности оставляем, они пока нужны
    @MockitoBean private JwtRequestFilter jwtRequestFilter;
    @MockitoBean private UserDetailsService userDetailsService;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    private Book savedBook;
    private Author savedAuthor;
    private Category savedCategory;
    private Publisher savedPublisher;
    private BookRequestDTO validRequestDTO;

    @BeforeEach
    void setUp() throws Exception {

        // 1. Создаем справочники в памяти (НЕ вызываем на них репозитории сразу)
        Author author = new Author("Лев Толстой", "Классик");
        Category category = new Category("Художественная литература");
        Publisher publisher = new Publisher("Эксмо");

        // 2. Создаем книгу и связываем объекты в памяти
        Book book = new Book("123-456", "War and Peace", "Classic epic novel");
        book.addAuthor(author);
        book.addCategory(category);
        book.addPublisher(publisher);

        // 3. Сохраняем книгу ОДНИМ вызовом. Благодаря CascadeType.PERSIST,
        // Hibernate сам поочередно создаст автора, категорию, издателя и свяжет таблицы!
        savedBook = bookRepository.save(book);

        // Достаем сгенерированные базой объекты для использования в validRequestDTO
        savedAuthor = savedBook.getAuthors().iterator().next();
        savedCategory = savedBook.getCategories().iterator().next();
        savedPublisher = savedBook.getPublishers().iterator().next();

        // 4. Подготавливаем валидный DTO для тестов POST
        validRequestDTO = new BookRequestDTO();
        validRequestDTO.setName("War and Peace");
        validRequestDTO.setIsbn("777-888");
        validRequestDTO.setDescription("Classic epic novel");
        validRequestDTO.setAuthorIds(Set.of(savedAuthor.getId()));
        validRequestDTO.setCategoryIds(Set.of(savedCategory.getId()));
        validRequestDTO.setPublisherIds(Set.of(savedPublisher.getId()));

        // Обход фильтра безопасности
        doAnswer(invocation -> {
            jakarta.servlet.ServletRequest request = invocation.getArgument(0);
            jakarta.servlet.ServletResponse response = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtRequestFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("GET /api/v1/books - Should return all books paginated")
    @WithMockUser
    void getAllBooks_WithoutParam_ShouldReturnPaginated() throws Exception {
        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                // Проверяем структуру пагинации Spring Data Page: $.content[0]
                .andExpect(jsonPath("$.content[0].id").value(savedBook.getId()))
                .andExpect(jsonPath("$.content[0].name").value("War and Peace"))
                .andExpect(jsonPath("$.content[0].isbn").value("123-456"));
    }

    @Test
    @DisplayName("GET /api/v1/books?keyword=... - Should search books by keyword")
    @WithMockUser
    void getAllBooks_WithParam_ShouldReturnFiltered() throws Exception {
        mockMvc.perform(get("/api/v1/books").param("keyword", "War"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(savedBook.getId()))
                .andExpect(jsonPath("$.content[0].name").value("War and Peace"));
    }

    @Test
    @DisplayName("GET /api/v1/books/{id} - Should return book by id")
    @WithMockUser
    void getBookById_ShouldReturnBook() throws Exception {
        mockMvc.perform(get("/api/v1/books/" + savedBook.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedBook.getId()))
                .andExpect(jsonPath("$.name").value("War and Peace"));
    }

    @Test
    @DisplayName("GET /api/v1/books/{id} - Should return 404 when book missing")
    @WithMockUser
    void getBookById_NotFound_ShouldReturn404() throws Exception {
        // Передаем несуществующий ID, чтобы глобальный обработчик исключений перехватил ошибку
        mockMvc.perform(get("/api/v1/books/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(ApiErrorCode.ENTITY_NOT_FOUND.getHttpStatus().value()))
                .andExpect(jsonPath("$.error").value(ApiErrorCode.ENTITY_NOT_FOUND.getHttpStatus().getReasonPhrase()))
                .andExpect(jsonPath("$.errorCode").value(ApiErrorCode.ENTITY_NOT_FOUND.getValue()));
    }

    @Test
    @DisplayName("POST /api/v1/books - Should create book when user is ADMIN")
    @WithMockUser(roles = "ADMIN")
    void createBook_AsAdmin_ShouldReturnCreated() throws Exception {
        mockMvc.perform(post("/api/v1/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isCreated())
                // База данных должна вернуть сгенерированный ID, проверяем его наличие
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("War and Peace"))
                .andExpect(jsonPath("$.isbn").value("777-888"));
    }

    @Test
    @DisplayName("POST /api/v1/books - Should return 400 when validation fails")
    @WithMockUser(roles = "ADMIN")
    void createBook_InvalidDto_ShouldReturnBadRequest() throws Exception {
        BookRequestDTO invalidDto = new BookRequestDTO();
        invalidDto.setName(""); // Пустое имя нарушает @NotEmpty

        mockMvc.perform(post("/api/v1/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ApiErrorCode.VALIDATION_FAILED.getHttpStatus().value()))
                .andExpect(jsonPath("$.error").value(ApiErrorCode.VALIDATION_FAILED.getHttpStatus().getReasonPhrase()))
                .andExpect(jsonPath("$.message").value(ApiErrorCode.VALIDATION_FAILED.getDefaultMessage()))
                .andExpect(jsonPath("$.errorCode").value(ApiErrorCode.VALIDATION_FAILED.getValue()))
                .andExpect(jsonPath("$.validationErrors.name").exists());
    }

    @Test
    @DisplayName("POST /api/v1/books - Should return 403 when user is not ADMIN")
    @WithMockUser(roles = "USER")
    void createBook_AsUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(ApiErrorCode.ACCESS_DENIED.getHttpStatus().value()))
                .andExpect(jsonPath("$.error").value(ApiErrorCode.ACCESS_DENIED.getHttpStatus().getReasonPhrase()))
                .andExpect(jsonPath("$.errorCode").value(ApiErrorCode.ACCESS_DENIED.getValue()));
    }

    @Test
    @DisplayName("PUT /api/v1/books/{id} - Should update book when user is ADMIN")
    @WithMockUser(roles = "ADMIN")
    void updateBook_AsAdmin_ShouldReturnOk() throws Exception {
        // Подготавливаем обновленные данные для существующей книги
        BookRequestDTO updateDto = new BookRequestDTO();
        updateDto.setName("War and Peace - New Edition");
        updateDto.setIsbn(savedBook.getIsbn()); // Оставляем прежний уникальный ISBN
        updateDto.setDescription("Updated description");
        updateDto.setAuthorIds(Set.of(savedAuthor.getId()));
        updateDto.setCategoryIds(Set.of(savedCategory.getId()));
        updateDto.setPublisherIds(Set.of(savedPublisher.getId()));

        mockMvc.perform(put("/api/v1/books/" + savedBook.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("War and Peace - New Edition"));
    }

    @Test
    @DisplayName("DELETE /api/v1/books/{id} - Should delete book when user is ADMIN")
    @WithMockUser(roles = "ADMIN")
    void deleteBook_AsAdmin_ShouldReturnNoContent() throws Exception {
        // 1. Удаляем книгу через API
        mockMvc.perform(delete("/api/v1/books/" + savedBook.getId()).with(csrf()))
                .andExpect(status().isNoContent());

        // 2. Проверяем это через HTTP слой
        // Если книга удалена, повторный GET-запрос обязан вернуть 404 Not Found со строгим контрактом
        mockMvc.perform(get("/api/v1/books/" + savedBook.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(ApiErrorCode.ENTITY_NOT_FOUND.getValue()));
    }

    @Test
    @DisplayName("DELETE /api/v1/books/{id} - Should return 403 when user is not ADMIN")
    @WithMockUser(roles = "USER")
    void deleteBook_AsUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/books/" + savedBook.getId()).with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(ApiErrorCode.ACCESS_DENIED.getHttpStatus().value()))
                .andExpect(jsonPath("$.error").value(ApiErrorCode.ACCESS_DENIED.getHttpStatus().getReasonPhrase()))
                .andExpect(jsonPath("$.errorCode").value(ApiErrorCode.ACCESS_DENIED.getValue()));

        // Книга должна остаться в базе данных PostgreSQL
        assertThat(bookRepository.existsById(savedBook.getId())).isTrue();
    }
}