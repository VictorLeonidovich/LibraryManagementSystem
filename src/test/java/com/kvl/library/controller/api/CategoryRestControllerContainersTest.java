package com.kvl.library.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvl.library.controller.BaseWebContainersTest;
import com.kvl.library.dto.CategoryRequestDTO;
import com.kvl.library.entity.Category;
import com.kvl.library.exception.ApiErrorCode;
import com.kvl.library.repository.CategoryRepository;
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

@DisplayName("CategoryRestController Integration Tests (PostgreSQL Testcontainers)")
class CategoryRestControllerContainersTest extends BaseWebContainersTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CategoryRepository categoryRepository;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Category savedCategory;
    private CategoryRequestDTO validRequestDTO;

    @BeforeEach
    void setUp() throws Exception {
        // 1. Сохраняем реальную категорию в PostgreSQL через репозиторий
        Category category = new Category();
        category.setName("Sci-Fi");
        savedCategory = categoryRepository.save(category);

        // 2. Готовим уникальный DTO для тестов создания (чтобы не нарушать unique constraint "categories_name_key")
        validRequestDTO = new CategoryRequestDTO();
        validRequestDTO.setName("Drama");

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
    @DisplayName("GET /api/v1/categories - Should return all categories paginated")
    @WithMockUser
    void getAllCategories_WithoutParam_ShouldReturnPaginated() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                // Добавили [0] для указания на первый элемент в массиве content
                .andExpect(jsonPath("$.content[0].id").value(savedCategory.getId()))
                .andExpect(jsonPath("$.content[0].name").value("Sci-Fi"));
    }

    @Test
    @DisplayName("GET /api/v1/categories?name=... - Should search categories by name")
    @WithMockUser
    void getAllCategories_WithParam_ShouldReturnFiltered() throws Exception {
        mockMvc.perform(get("/api/v1/categories").param("name", "Sci"))
                .andExpect(status().isOk())
                // Добавили [0] для указания на первый элемент в массиве content
                .andExpect(jsonPath("$.content[0].name").value("Sci-Fi"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} - Should return category by id")
    @WithMockUser
    void getCategoryById_ShouldReturnCategory() throws Exception {
        mockMvc.perform(get("/api/v1/categories/" + savedCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCategory.getId()))
                .andExpect(jsonPath("$.name").value("Sci-Fi"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} - Should return 404 when missing")
    @WithMockUser
    void getCategoryById_NotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/categories/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(ApiErrorCode.ENTITY_NOT_FOUND.getHttpStatus().value()))
                .andExpect(jsonPath("$.error").value(ApiErrorCode.ENTITY_NOT_FOUND.getHttpStatus().getReasonPhrase()))
                .andExpect(jsonPath("$.errorCode").value(ApiErrorCode.ENTITY_NOT_FOUND.getValue()));
    }

    @Test
    @DisplayName("POST /api/v1/categories - Should create category when user is ADMIN")
    @WithMockUser(roles = "ADMIN")
    void createCategory_AsAdmin_ShouldReturnCreated() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Drama"));
    }

    @Test
    @DisplayName("POST /api/v1/categories - Should return 400 when validation fails")
    @WithMockUser(roles = "ADMIN")
    void createCategory_InvalidDto_ShouldReturnBadRequest() throws Exception {
        CategoryRequestDTO invalidDto = new CategoryRequestDTO();
        invalidDto.setName(""); // Ошибка валидации: пустое имя нарушает @NotEmpty

        mockMvc.perform(post("/api/v1/categories")
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
    @DisplayName("POST /api/v1/categories - Should return 403 when user is not ADMIN")
    @WithMockUser(roles = "USER")
    void createCategory_AsUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(ApiErrorCode.ACCESS_DENIED.getHttpStatus().value()))
                .andExpect(jsonPath("$.error").value(ApiErrorCode.ACCESS_DENIED.getHttpStatus().getReasonPhrase()))
                .andExpect(jsonPath("$.errorCode").value(ApiErrorCode.ACCESS_DENIED.getValue()));
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} - Should update category when ADMIN")
    @WithMockUser(roles = "ADMIN")
    void updateCategory_AsAdmin_ShouldReturnOk() throws Exception {
        CategoryRequestDTO updateDto = new CategoryRequestDTO();
        updateDto.setName("Sci-Fi - Modified");

        mockMvc.perform(put("/api/v1/categories/" + savedCategory.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCategory.getId()))
                .andExpect(jsonPath("$.name").value("Sci-Fi - Modified"));
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - Should delete category when ADMIN")
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_AsAdmin_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/" + savedCategory.getId()).with(csrf()))
                .andExpect(status().isNoContent());

        // Проверяем удаление сквозным образом — повторный GET запрос обязан вернуть 404
        mockMvc.perform(get("/api/v1/categories/" + savedCategory.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(ApiErrorCode.ENTITY_NOT_FOUND.getValue()));
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - Should return 403 when user is not ADMIN")
    @WithMockUser(roles = "USER")
    void deleteCategory_AsUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/" + savedCategory.getId()).with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(ApiErrorCode.ACCESS_DENIED.getHttpStatus().value()))
                .andExpect(jsonPath("$.error").value(ApiErrorCode.ACCESS_DENIED.getHttpStatus().getReasonPhrase()))
                .andExpect(jsonPath("$.errorCode").value(ApiErrorCode.ACCESS_DENIED.getValue()));

        // Проверяем через репозиторий, что категория осталась нетронутой в PostgreSQL
        assertThat(categoryRepository.existsById(savedCategory.getId())).isTrue();
    }
}