package com.kvl.library.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvl.library.dto.CategoryRequestDTO;
import com.kvl.library.dto.CategoryResponseDTO;
import com.kvl.library.entity.Category;
import com.kvl.library.exception.EntityNotFoundException;
import com.kvl.library.mapper.CategoryMapper;
import com.kvl.library.security.JwtRequestFilter;
import com.kvl.library.service.core.CategoryService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryRestController.class)
@ActiveProfiles("test")
@EnableMethodSecurity
@DisplayName("CategoryRestController Unit Tests")
class CategoryRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private CategoryMapper categoryMapper;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Category testCategory;
    private CategoryResponseDTO testResponseDTO;
    private CategoryRequestDTO validRequestDTO;

    @BeforeEach
    void setUp() throws Exception {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Sci-Fi");

        testResponseDTO = new CategoryResponseDTO();
        testResponseDTO.setId(1L);
        testResponseDTO.setName("Sci-Fi");

        validRequestDTO = new CategoryRequestDTO();
        validRequestDTO.setName("Sci-Fi");

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
        Page<Category> page = new PageImpl<>(Collections.singletonList(testCategory));
        when(categoryService.findAllCategories(any(Pageable.class))).thenReturn(page);
        when(categoryMapper.toResponseDTO(testCategory)).thenReturn(testResponseDTO);

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Sci-Fi"));
    }

    @Test
    @DisplayName("GET /api/v1/categories?name=... - Should search categories by name")
    @WithMockUser
    void getAllCategories_WithParam_ShouldReturnFiltered() throws Exception {
        Page<Category> page = new PageImpl<>(Collections.singletonList(testCategory));
        when(categoryService.searchCategoriesByName(eq("Sci"), any(Pageable.class))).thenReturn(page);
        when(categoryMapper.toResponseDTO(testCategory)).thenReturn(testResponseDTO);

        mockMvc.perform(get("/api/v1/categories").param("name", "Sci"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Sci-Fi"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} - Should return category by id")
    @WithMockUser
    void getCategoryById_ShouldReturnCategory() throws Exception {
        when(categoryService.findCategoryById(1L)).thenReturn(testCategory);
        when(categoryMapper.toResponseDTO(testCategory)).thenReturn(testResponseDTO);

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Sci-Fi"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} - Should return 404 when missing")
    @WithMockUser
    void getCategoryById_NotFound_ShouldReturn404() throws Exception {
        when(categoryService.findCategoryById(99L)).thenThrow(new EntityNotFoundException("Category not found"));

        mockMvc.perform(get("/api/v1/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/v1/categories - Should create category when user is ADMIN")
    @WithMockUser(roles = "ADMIN")
    void createCategory_AsAdmin_ShouldReturnCreated() throws Exception {
        when(categoryMapper.toEntity(any(CategoryRequestDTO.class))).thenReturn(testCategory);
        doNothing().when(categoryService).createCategory(any(Category.class));
        when(categoryMapper.toResponseDTO(testCategory)).thenReturn(testResponseDTO);

        mockMvc.perform(post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("POST /api/v1/categories - Should return 400 when validation fails")
    @WithMockUser(roles = "ADMIN")
    void createCategory_InvalidDto_ShouldReturnBadRequest() throws Exception {
        CategoryRequestDTO invalidDto = new CategoryRequestDTO();
        invalidDto.setName(""); // Ошибка валдиации

        mockMvc.perform(post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
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
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} - Should update category when ADMIN")
    @WithMockUser(roles = "ADMIN")
    void updateCategory_AsAdmin_ShouldReturnOk() throws Exception {
        when(categoryService.findCategoryById(1L)).thenReturn(testCategory);
        doNothing().when(categoryMapper).updateEntityFromDto(any(CategoryRequestDTO.class), any(Category.class));
        doNothing().when(categoryService).updateCategory(any(Category.class));
        when(categoryMapper.toResponseDTO(testCategory)).thenReturn(testResponseDTO);

        mockMvc.perform(put("/api/v1/categories/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - Should delete category when ADMIN")
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_AsAdmin_ShouldReturnNoContent() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/v1/categories/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - Should return 403 when user is not ADMIN")
    @WithMockUser(roles = "USER")
    void deleteCategory_AsUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/1").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}