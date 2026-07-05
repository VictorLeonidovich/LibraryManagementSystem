package com.kvl.library.controller.ui;

import com.kvl.library.entity.Category;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@ActiveProfiles("test")
@DisplayName("CategoryController Unit Tests")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Category testCategory;

    @BeforeEach
    void setUp() throws Exception {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Sci-Fi");

        // Обучаем публичный метод доходить до конца и вызывать следующую цепочку фильтров
        doAnswer(invocation -> {
            jakarta.servlet.ServletRequest request = invocation.getArgument(0);
            jakarta.servlet.ServletResponse response = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtRequestFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("GET /categories - Should return categories template with paginated data")
    @WithMockUser
    void findAllCategories_ShouldReturnTemplateWithData() throws Exception {
        Page<Category> categoryPage = new PageImpl<>(Collections.singletonList(testCategory));
        when(categoryService.findAllCategories(any(Pageable.class))).thenReturn(categoryPage);

        mockMvc.perform(get("/categories")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("categories"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("totalItems", 1L))
                .andExpect(model().attribute("size", 5));

        verify(categoryService, times(1)).findAllCategories(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /remove-category/{id} - Should delete category and redirect to categories list")
    @WithMockUser
    void removeCategory_ShouldDeleteAndRedirect() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(get("/remove-category/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));

        verify(categoryService, times(1)).deleteCategory(1L);
    }

    @Test
    @DisplayName("GET /update-category/{id} - Should return update template with category data")
    @WithMockUser
    void updateCategory_ShouldReturnUpdateForm() throws Exception {
        when(categoryService.findCategoryById(1L)).thenReturn(testCategory);

        mockMvc.perform(get("/update-category/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-category"))
                .andExpect(model().attribute("category", testCategory));

        verify(categoryService, times(1)).findCategoryById(1L);
    }

    @Test
    @DisplayName("POST /save-category/{id} - Should update category and redirect when data is valid")
    @WithMockUser
    void updateCategory_ShouldSaveAndRedirect_WhenValid() throws Exception {
        doNothing().when(categoryService).updateCategory(any(Category.class));

        mockMvc.perform(post("/save-category/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", "1")
                        .param("name", "Fantasy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));

        verify(categoryService, times(1)).updateCategory(any(Category.class));
    }

    @Test
    @DisplayName("POST /save-category/{id} - Should return update template when validation fails")
    @WithMockUser
    void updateCategory_ShouldReturnUpdateForm_WhenInvalid() throws Exception {
        mockMvc.perform(post("/save-category/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", "1")
                        .param("name", "")) // Пустая строка для вызова ошибок валидации
                .andExpect(status().isOk())
                .andExpect(view().name("update-category"))
                .andExpect(model().hasErrors());

        verify(categoryService, never()).updateCategory(any(Category.class));
    }

    @Test
    @DisplayName("GET /add-category - Should return add-category template with empty category object")
    @WithMockUser
    void addCategory_ShouldReturnAddForm() throws Exception {
        mockMvc.perform(get("/add-category"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-category"))
                .andExpect(model().attributeExists("category"));
    }

    @Test
    @DisplayName("POST /save-category - Should create category and redirect when data is valid")
    @WithMockUser
    void saveCategory_ShouldCreateAndRedirect_WhenValid() throws Exception {
        doNothing().when(categoryService).createCategory(any(Category.class));

        mockMvc.perform(post("/save-category")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Detective"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));

        verify(categoryService, times(1)).createCategory(any(Category.class));
    }

    @Test
    @DisplayName("POST /save-category - Should return add-category template when validation fails")
    @WithMockUser
    void saveCategory_ShouldReturnAddForm_WhenInvalid() throws Exception {
        mockMvc.perform(post("/save-category")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "")) // Ошибка валидации
                .andExpect(status().isOk())
                .andExpect(view().name("add-category"))
                .andExpect(model().hasErrors());

        verify(categoryService, never()).createCategory(any(Category.class));
    }
}