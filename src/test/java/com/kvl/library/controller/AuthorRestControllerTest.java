package com.kvl.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvl.library.dto.AuthorRequestDTO;
import com.kvl.library.dto.AuthorResponseDTO;
import com.kvl.library.entity.Author;
import com.kvl.library.exception.EntityNotFoundException;
import com.kvl.library.mapper.AuthorMapper;
import com.kvl.library.security.JwtRequestFilter;
import com.kvl.library.service.AuthorService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthorRestController.class)
@ActiveProfiles("test")
@EnableMethodSecurity // Включаем обработку @PreAuthorize в контексте этого теста
@DisplayName("AuthorRestController Unit Tests with Global Exception Handling")
class AuthorRestControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private AuthorMapper authorMapper;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Author testAuthor;
    private AuthorResponseDTO testResponseDTO;
    private AuthorRequestDTO validRequestDTO;

    @BeforeEach
    void setUp() throws Exception {
        testAuthor = new Author();
        testAuthor.setId(1L);
        testAuthor.setName("Leo Tolstoy");
        testAuthor.setDescription("Literature classic");

        testResponseDTO = new AuthorResponseDTO();
        testResponseDTO.setId(1L);
        testResponseDTO.setName("Leo Tolstoy");
        testResponseDTO.setDescription("Literature classic");

        validRequestDTO = new AuthorRequestDTO();
        validRequestDTO.setName("Leo Tolstoy");
        validRequestDTO.setDescription("Literature classic");

        // Пропуск JWT фильтра безопасности по цепочке дальше
        doAnswer(invocation -> {
            jakarta.servlet.ServletRequest request = invocation.getArgument(0);
            jakarta.servlet.ServletResponse response = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtRequestFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("GET /api/v1/authors - Should return all authors paginated (no search param)")
    @WithMockUser
    void getAllAuthors_WithoutSearchParam_ShouldReturnPaginatedList() throws Exception {
        Page<Author> authorPage = new PageImpl<>(Collections.singletonList(testAuthor));

        when(authorService.findAllAuthors(any(Pageable.class))).thenReturn(authorPage);
        when(authorMapper.toResponseDTO(testAuthor)).thenReturn(testResponseDTO);

        mockMvc.perform(get("/api/v1/authors")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Leo Tolstoy"))
                .andExpect(jsonPath("$.content[0].description").value("Literature classic"));
    }

    @Test
    @DisplayName("GET /api/v1/authors - Should search authors by name when param is provided")
    @WithMockUser
    void getAllAuthors_WithSearchParam_ShouldReturnFilteredList() throws Exception {
        Page<Author> authorPage = new PageImpl<>(Collections.singletonList(testAuthor));

        when(authorService.searchAuthorsByName(eq("Tolstoy"), any(Pageable.class))).thenReturn(authorPage);
        when(authorMapper.toResponseDTO(testAuthor)).thenReturn(testResponseDTO);

        mockMvc.perform(get("/api/v1/authors")
                        .param("name", "Tolstoy")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Leo Tolstoy"));
    }

    @Test
    @DisplayName("GET /api/v1/authors/{id} - Should return specific author by id")
    @WithMockUser
    void getAuthorById_ShouldReturnAuthor() throws Exception {
        when(authorService.findAuthorById(1L)).thenReturn(testAuthor);
        when(authorMapper.toResponseDTO(testAuthor)).thenReturn(testResponseDTO);

        mockMvc.perform(get("/api/v1/authors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Leo Tolstoy"));
    }

    @Test
    @DisplayName("GET /api/v1/authors/{id} - Should return 404 Not Found via ApiGlobalExceptionHandler when entity missing")
    @WithMockUser
    void getAuthorById_WhenNotFound_ShouldReturn404ApiError() throws Exception {
        when(authorService.findAuthorById(99L)).thenThrow(new EntityNotFoundException("Author not found with id: 99"));

        mockMvc.perform(get("/api/v1/authors/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Author not found with id: 99"))
                .andExpect(jsonPath("$.path").value("/api/v1/authors/99"));
    }

    @Test
    @DisplayName("POST /api/v1/authors - Should create author when user is ADMIN and DTO is valid")
    @WithMockUser(roles = "ADMIN")
    void createAuthor_AsAdmin_WithValidDto_ShouldReturnCreated() throws Exception {
        when(authorMapper.toEntity(any(AuthorRequestDTO.class))).thenReturn(testAuthor);
        doNothing().when(authorService).createAuthor(any(Author.class));
        when(authorMapper.toResponseDTO(testAuthor)).thenReturn(testResponseDTO);

        mockMvc.perform(post("/api/v1/authors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("POST /api/v1/authors - Should return 400 Bad Request with validation errors structure")
    @WithMockUser(roles = "ADMIN")
    void createAuthor_WithInvalidDto_ShouldReturn400WithDetails() throws Exception {
        AuthorRequestDTO invalidRequestDTO = new AuthorRequestDTO();
        invalidRequestDTO.setName("");
        invalidRequestDTO.setDescription("A");

        mockMvc.perform(post("/api/v1/authors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.name").exists()) // Проверяем просто наличие ошибки для имени
                .andExpect(jsonPath("$.validationErrors.description").value("Описание должно быть длиной от 2 до 250 символов"));

        verify(authorService, never()).createAuthor(any(Author.class));
    }

    @Test
    @DisplayName("POST /api/v1/authors - Should return 403 Forbidden when user is not ADMIN")
    @WithMockUser(roles = "USER")
    void createAuthor_AsUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/authors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("PUT /api/v1/authors/{id} - Should update author when user is ADMIN and DTO is valid")
    @WithMockUser(roles = "ADMIN")
    void updateAuthor_AsAdmin_WithValidDto_ShouldReturnOk() throws Exception {
        when(authorService.findAuthorById(1L)).thenReturn(testAuthor);
        doNothing().when(authorMapper).updateEntityFromDto(any(AuthorRequestDTO.class), any(Author.class));
        doNothing().when(authorService).updateAuthor(any(Author.class));
        when(authorMapper.toResponseDTO(testAuthor)).thenReturn(testResponseDTO);

        mockMvc.perform(put("/api/v1/authors/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("DELETE /api/v1/authors/{id} - Should delete author when user is ADMIN")
    @WithMockUser(roles = "ADMIN")
    void deleteAuthor_AsAdmin_ShouldReturnNoContent() throws Exception {
        doNothing().when(authorService).deleteAuthor(1L);
        mockMvc.perform(delete("/api/v1/authors/1").with(csrf())).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/authors/{id} - Should return 403 Forbidden when user is not ADMIN")
    @WithMockUser(roles = "USER")
    void deleteAuthor_AsUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/authors/1")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }
}