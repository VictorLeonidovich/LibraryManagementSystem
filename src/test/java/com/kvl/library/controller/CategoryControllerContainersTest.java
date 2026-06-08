package com.kvl.library.controller;

import com.kvl.library.entity.Category;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("CategoryController Thymeleaf Integration Tests (PostgreSQL Testcontainers)")
class CategoryControllerContainersTest extends BaseWebContainersTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Category savedCategory;

    @BeforeEach
    void setUp() throws Exception {
        // Базовый класс BaseWebContainersTest автоматически очистит базу перед тестом

        // Сохраняем реальную категорию в PostgreSQL через репозиторий
        Category category = new Category();
        category.setName("Sci-Fi");
        savedCategory = categoryRepository.save(category);

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
    @DisplayName("GET /categories - Should return categories template with paginated data from PostgreSQL")
    @WithMockUser
    void findAllCategories_ShouldReturnTemplateWithData() throws Exception {
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
    }

    @Test
    @DisplayName("GET /remove-category/{id} - Should delete category from PostgreSQL and redirect to list")
    @WithMockUser
    void removeCategory_ShouldDeleteAndRedirect() throws Exception {
        mockMvc.perform(get("/remove-category/" + savedCategory.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));

        // Честно проверяем в PostgreSQL, что категории больше нет
        assertThat(categoryRepository.existsById(savedCategory.getId())).isFalse();
    }

    @Test
    @DisplayName("GET /update-category/{id} - Should return update template with category data from PostgreSQL")
    @WithMockUser
    void updateCategory_ShouldReturnUpdateForm() throws Exception {
        mockMvc.perform(get("/update-category/" + savedCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("update-category"))
                .andExpect(model().attributeExists("category"));
    }

    @Test
    @DisplayName("POST /save-category/{id} - Should update category in PostgreSQL and redirect when data is valid")
    @WithMockUser
    void updateCategory_ShouldSaveAndRedirect_WhenValid() throws Exception {
        mockMvc.perform(post("/save-category/" + savedCategory.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", savedCategory.getId().toString())
                        .param("name", "Fantasy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));

        // Проверяем, что в базе PostgreSQL имя действительно обновилось
        Category updatedCategory = categoryRepository.findById(savedCategory.getId()).orElseThrow();
        assertThat(updatedCategory.getName()).isEqualTo("Fantasy");
    }

    @Test
    @DisplayName("POST /save-category/{id} - Should return update template when validation fails")
    @WithMockUser
    void updateCategory_ShouldReturnUpdateForm_WhenInvalid() throws Exception {
        mockMvc.perform(post("/save-category/" + savedCategory.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", savedCategory.getId().toString())
                        .param("name", "")) // Пустая строка триггерит Jakarta Validation
                .andExpect(status().isOk())
                .andExpect(view().name("update-category"))
                .andExpect(model().hasErrors());
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
    @DisplayName("POST /save-category - Should create category in PostgreSQL and redirect when data is valid")
    @WithMockUser
    void saveCategory_ShouldCreateAndRedirect_WhenValid() throws Exception {
        mockMvc.perform(post("/save-category")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Detective"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));
    }

    @Test
    @DisplayName("POST /save-category - Should return add-category template when validation fails")
    @WithMockUser
    void saveCategory_ShouldReturnAddForm_WhenInvalid() throws Exception {
        mockMvc.perform(post("/save-category")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "")) // Ошибка валидации: пустое имя
                .andExpect(status().isOk())
                .andExpect(view().name("add-category"))
                .andExpect(model().hasErrors());
    }
}