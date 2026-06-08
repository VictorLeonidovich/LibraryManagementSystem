package com.kvl.library.controller;

import com.kvl.library.entity.Author;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("AuthorController Thymeleaf Integration Tests (PostgreSQL Testcontainers)")
class AuthorControllerContainersTest extends BaseWebContainersTest {

    @Autowired
    private AuthorRepository authorRepository;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Author savedAuthor;

    @BeforeEach
    void setUp() throws Exception {
        // Базовый класс сам очистит все таблицы перед тестом

        // Сохраняем реального автора в PostgreSQL через репозиторий
        Author author = new Author();
        author.setName("Leo Tolstoy");
        author.setDescription("Literature classic");
        savedAuthor = authorRepository.save(author);

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
    @DisplayName("GET /authors - Should return authors template with paginated data from PostgreSQL")
    @WithMockUser
    void findAllAuthors_ShouldReturnTemplateWithData() throws Exception {
        mockMvc.perform(get("/authors")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("authors"))
                .andExpect(model().attributeExists("authors"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("totalItems", 1L))
                .andExpect(model().attribute("size", 5));
    }

    @Test
    @DisplayName("GET /remove-author/{id} - Should delete author from PostgreSQL and redirect to list")
    @WithMockUser
    void removeAuthor_ShouldDeleteAndRedirect() throws Exception {
        mockMvc.perform(get("/remove-author/" + savedAuthor.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/authors"));

        // Честно проверяем в PostgreSQL, что автора больше нет
        assertThat(authorRepository.existsById(savedAuthor.getId())).isFalse();
    }

    @Test
    @DisplayName("GET /update-author/{id} - Should return update template with author data from PostgreSQL")
    @WithMockUser
    void updateAuthor_ShouldReturnUpdateForm() throws Exception {
        mockMvc.perform(get("/update-author/" + savedAuthor.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("update-author"))
                .andExpect(model().attributeExists("author"));
    }

    @Test
    @DisplayName("POST /save-author/{id} - Should update author in PostgreSQL and redirect when data is valid")
    @WithMockUser
    void updateAuthor_ShouldSaveAndRedirect_WhenValid() throws Exception {
        mockMvc.perform(post("/save-author/" + savedAuthor.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", savedAuthor.getId().toString())
                        .param("name", "Anton Chekhov")
                        .param("description", "Famous playwright"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/authors"));

        // Дополнительно проверяем, что в базе PostgreSQL имя действительно обновилось
        Author updatedAuthor = authorRepository.findById(savedAuthor.getId()).orElseThrow();
        assertThat(updatedAuthor.getName()).isEqualTo("Anton Chekhov");
    }

    @Test
    @DisplayName("POST /save-author/{id} - Should return update template when validation fails")
    @WithMockUser
    void updateAuthor_ShouldReturnUpdateForm_WhenInvalid() throws Exception {
        mockMvc.perform(post("/save-author/" + savedAuthor.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", savedAuthor.getId().toString())
                        .param("name", "") // Пустое имя триггерит Jakarta Validation
                        .param("description", "Some description"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-author"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("GET /add-author - Should return add-author template with empty author object")
    @WithMockUser
    void addAuthor_ShouldReturnAddForm() throws Exception {
        mockMvc.perform(get("/add-author"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-author"))
                .andExpect(model().attributeExists("author"));
    }

    @Test
    @DisplayName("POST /save-author - Should create author in PostgreSQL and redirect when data is valid")
    @WithMockUser
    void saveAuthor_ShouldCreateAndRedirect_WhenValid() throws Exception {
        mockMvc.perform(post("/save-author")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Fyodor Dostoevsky")
                        .param("description", "Great writer"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/authors"));
    }

    @Test
    @DisplayName("POST /save-author - Should return add-author template when validation fails")
    @WithMockUser
    void saveAuthor_ShouldReturnAddForm_WhenInvalid() throws Exception {
        mockMvc.perform(post("/save-author")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "") // Нарушает @NotEmpty
                        .param("description", "Great writer"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-author"))
                .andExpect(model().hasErrors());
    }
}