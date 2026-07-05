package com.kvl.library.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvl.library.dto.BookRequestDTO;
import com.kvl.library.dto.BookResponseDTO;
import com.kvl.library.entity.Author;
import com.kvl.library.entity.Book;
import com.kvl.library.entity.Category;
import com.kvl.library.entity.Publisher;
import com.kvl.library.exception.EntityNotFoundException;
import com.kvl.library.mapper.BookMapper;
import com.kvl.library.security.JwtRequestFilter;
import com.kvl.library.service.core.AuthorService;
import com.kvl.library.service.book.BookService;
import com.kvl.library.service.core.CategoryService;
import com.kvl.library.service.core.PublisherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookRestController.class)
@ActiveProfiles("test")
@EnableMethodSecurity
@DisplayName("BookRestController Unit Tests")
class BookRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private PublisherService publisherService;

    @MockitoBean
    private BookMapper bookMapper;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Book testBook;
    private BookResponseDTO testResponseDTO;
    private BookRequestDTO validRequestDTO;

    @BeforeEach
    void setUp() throws Exception {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setName("War and Peace");
        testBook.setIsbn("123-456");
        testBook.setDescription("Classic epic novel");

        testResponseDTO = new BookResponseDTO();
        testResponseDTO.setId(1L);
        testResponseDTO.setName("War and Peace");
        testResponseDTO.setIsbn("123-456");
        testResponseDTO.setDescription("Classic epic novel");

        validRequestDTO = new BookRequestDTO();
        validRequestDTO.setName("War and Peace");
        validRequestDTO.setIsbn("123-456");
        validRequestDTO.setDescription("Classic epic novel");
        validRequestDTO.setAuthorIds(Set.of(1L));
        validRequestDTO.setCategoryIds(Set.of(1L));
        validRequestDTO.setPublisherIds(Set.of(1L));

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
        Page<Book> page = new PageImpl<>(Collections.singletonList(testBook));
        when(bookService.findAllBooks(any(Pageable.class))).thenReturn(page);
        when(bookMapper.toResponseDTO(testBook)).thenReturn(testResponseDTO);

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("War and Peace"));
    }

    @Test
    @DisplayName("GET /api/v1/books?keyword=... - Should search books by keyword")
    @WithMockUser
    void getAllBooks_WithParam_ShouldReturnFiltered() throws Exception {
        Page<Book> page = new PageImpl<>(Collections.singletonList(testBook));
        when(bookService.searchBooks(eq("War"), any(Pageable.class))).thenReturn(page);
        when(bookMapper.toResponseDTO(testBook)).thenReturn(testResponseDTO);

        mockMvc.perform(get("/api/v1/books").param("keyword", "War"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("War and Peace"));
    }

    @Test
    @DisplayName("GET /api/v1/books/{id} - Should return book by id")
    @WithMockUser
    void getBookById_ShouldReturnBook() throws Exception {
        when(bookService.findBookById(1L)).thenReturn(testBook);
        when(bookMapper.toResponseDTO(testBook)).thenReturn(testResponseDTO);

        mockMvc.perform(get("/api/v1/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("War and Peace"));
    }

    @Test
    @DisplayName("GET /api/v1/books/{id} - Should return 404 when book missing")
    @WithMockUser
    void getBookById_NotFound_ShouldReturn404() throws Exception {
        when(bookService.findBookById(99L)).thenThrow(new EntityNotFoundException("Book not found"));

        mockMvc.perform(get("/api/v1/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/v1/books - Should create book when user is ADMIN")
    @WithMockUser(roles = "ADMIN")
    void createBook_AsAdmin_ShouldReturnCreated() throws Exception {
        when(bookMapper.toEntity(any(BookRequestDTO.class))).thenReturn(testBook);

        // Заглушки для внутренних вызовов mapRelations()
        when(authorService.findAuthorById(1L)).thenReturn(new Author());
        when(categoryService.findCategoryById(1L)).thenReturn(new Category());
        when(publisherService.findPublisherById(1L)).thenReturn(new Publisher());

        doNothing().when(bookService).createBook(any(Book.class));
        when(bookMapper.toResponseDTO(testBook)).thenReturn(testResponseDTO);

        mockMvc.perform(post("/api/v1/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("POST /api/v1/books - Should return 400 when validation fails")
    @WithMockUser(roles = "ADMIN")
    void createBook_InvalidDto_ShouldReturnBadRequest() throws Exception {
        BookRequestDTO invalidDto = new BookRequestDTO();
        invalidDto.setName(""); // Пустое имя

        mockMvc.perform(post("/api/v1/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
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
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("PUT /api/v1/books/{id} - Should update book when user is ADMIN")
    @WithMockUser(roles = "ADMIN")
    void updateBook_AsAdmin_ShouldReturnOk() throws Exception {
        when(bookService.findBookById(1L)).thenReturn(testBook);
        doNothing().when(bookMapper).updateEntityFromDto(any(BookRequestDTO.class), any(Book.class));

        when(authorService.findAuthorById(1L)).thenReturn(new Author());
        when(categoryService.findCategoryById(1L)).thenReturn(new Category());
        when(publisherService.findPublisherById(1L)).thenReturn(new Publisher());

        doNothing().when(bookService).updateBook(any(Book.class));
        when(bookMapper.toResponseDTO(testBook)).thenReturn(testResponseDTO);

        mockMvc.perform(put("/api/v1/books/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/books/{id} - Should delete book when user is ADMIN")
    @WithMockUser(roles = "ADMIN")
    void deleteBook_AsAdmin_ShouldReturnNoContent() throws Exception {
        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/api/v1/books/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/books/{id} - Should return 403 when user is not ADMIN")
    @WithMockUser(roles = "USER")
    void deleteBook_AsUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/books/1").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}