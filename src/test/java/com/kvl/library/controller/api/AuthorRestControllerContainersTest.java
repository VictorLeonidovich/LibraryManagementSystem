package com.kvl.library.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvl.library.controller.BaseWebContainersTest;
import com.kvl.library.dto.AuthorRequestDTO;
import com.kvl.library.entity.Author;
import com.kvl.library.exception.ApiErrorCode;
import com.kvl.library.repository.AuthorRepository;
import com.kvl.library.security.JwtRequestFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AuthorRestController Integration Tests (PostgreSQL Testcontainers)")
class AuthorRestControllerContainersTest extends BaseWebContainersTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AuthorRepository authorRepository;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Author savedAuthor;
    private AuthorRequestDTO validRequestDTO;

    @BeforeEach
    void setUp() throws Exception {
        // 1. Сохраняем реального автора в PostgreSQL через репозиторий
        Author author = new Author("Leo Tolstoy", "Literature classic");
        savedAuthor = authorRepository.save(author);

        // 2. Готовим уникальный DTO для тестов создания (чтобы не нарушать unique constraint "authors_name_key")
        validRequestDTO = new AuthorRequestDTO();
        validRequestDTO.setName("Alexander Pushkin");
        validRequestDTO.setDescription("Great russian poet");

        // Пропуск JWT фильтра безопасности
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
        mockMvc.perform(get("/api/v1/authors")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(savedAuthor.getId()))
                .andExpect(jsonPath("$.content[0].name").value("Leo Tolstoy"))
                .andExpect(jsonPath("$.content[0].description").value("Literature classic"));
    }

    @Test
    @DisplayName("GET /api/v1/authors - Should search authors by name when param is provided")
    @WithMockUser
    void getAllAuthors_WithSearchParam_ShouldReturnFilteredList() throws Exception {
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
        mockMvc.perform(get("/api/v1/authors/" + savedAuthor.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedAuthor.getId()))
                .andExpect(jsonPath("$.name").value("Leo Tolstoy"));
    }

    @Test
    @DisplayName("GET /api/v1/authors/{id} - Should return 404 Not Found via ApiGlobalExceptionHandler when entity missing")
    @WithMockUser
    void getAuthorById_WhenNotFound_ShouldReturn404ApiError() throws Exception {
        mockMvc.perform(get("/api/v1/authors/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(ApiErrorCode.ENTITY_NOT_FOUND.getHttpStatus().value()))
                .andExpect(jsonPath("$.error").value(ApiErrorCode.ENTITY_NOT_FOUND.getHttpStatus().getReasonPhrase()))
                .andExpect(jsonPath("$.errorCode").value(ApiErrorCode.ENTITY_NOT_FOUND.getValue()))
                .andExpect(jsonPath("$.path").value("/api/v1/authors/99999"));
    }

    @Test
    @DisplayName("POST /api/v1/authors - Should create author when user is ADMIN and DTO is valid")
    @WithMockUser(roles = "ADMIN")
    void createAuthor_AsAdmin_WithValidDto_ShouldReturnCreated() throws Exception {
        mockMvc.perform(post("/api/v1/authors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Alexander Pushkin"));
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
                .andExpect(jsonPath("$.status").value(ApiErrorCode.VALIDATION_FAILED.getHttpStatus().value()))
                .andExpect(jsonPath("$.error").value(ApiErrorCode.VALIDATION_FAILED.getHttpStatus().getReasonPhrase()))
                .andExpect(jsonPath("$.message").value(ApiErrorCode.VALIDATION_FAILED.getDefaultMessage()))
                .andExpect(jsonPath("$.errorCode").value(ApiErrorCode.VALIDATION_FAILED.getValue()))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.description").value("Описание должно быть длиной от 2 до 250 символов"));
    }

    @Test
    @DisplayName("POST /api/v1/authors - Should return 403 Forbidden with ACCESS_DENIED errorCode when user is not ADMIN")
    @WithMockUser(roles = "USER")
    void createAuthor_AsUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/authors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(ApiErrorCode.ACCESS_DENIED.getHttpStatus().value()))
                .andExpect(jsonPath("$.error").value(ApiErrorCode.ACCESS_DENIED.getHttpStatus().getReasonPhrase()))
                .andExpect(jsonPath("$.errorCode").value(ApiErrorCode.ACCESS_DENIED.getValue()));
    }

    @Test
    @DisplayName("PUT /api/v1/authors/{id} - Should update author when user is ADMIN and DTO is valid")
    @WithMockUser(roles = "ADMIN")
    void updateAuthor_AsAdmin_WithValidDto_ShouldReturnOk() throws Exception {
        AuthorRequestDTO updateDto = new AuthorRequestDTO();
        updateDto.setName("Leo Tolstoy - Modified");
        updateDto.setDescription("Literature classic - Updated description");

        mockMvc.perform(put("/api/v1/authors/" + savedAuthor.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedAuthor.getId()))
                .andExpect(jsonPath("$.name").value("Leo Tolstoy - Modified"));
    }

    @Test
    @DisplayName("DELETE /api/v1/authors/{id} - Should delete author when user is ADMIN")
    @WithMockUser(roles = "ADMIN")
    void deleteAuthor_AsAdmin_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/authors/" + savedAuthor.getId()).with(csrf()))
                .andExpect(status().isNoContent());

        // Для веб-тестов эталонной проверкой удаления является повторный HTTP GET-запрос
        mockMvc.perform(get("/api/v1/authors/" + savedAuthor.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(ApiErrorCode.ENTITY_NOT_FOUND.getValue()));
    }

    @Test
    @DisplayName("DELETE /api/v1/authors/{id} - Should return 403 Forbidden with ACCESS_DENIED errorCode when user is not ADMIN")
    @WithMockUser(roles = "USER")
    void deleteAuthor_AsUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/authors/" + savedAuthor.getId()).with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(ApiErrorCode.ACCESS_DENIED.getHttpStatus().value()))
                .andExpect(jsonPath("$.error").value(ApiErrorCode.ACCESS_DENIED.getHttpStatus().getReasonPhrase()))
                .andExpect(jsonPath("$.errorCode").value(ApiErrorCode.ACCESS_DENIED.getValue()));

        // Проверяем через репозиторий, что автор остался в безопасности в PostgreSQL
        assertThat(authorRepository.existsById(savedAuthor.getId())).isTrue();
    }
}