package com.kvl.library.controller;

import com.kvl.library.entity.Author;
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

@WebMvcTest(AuthorController.class)
@ActiveProfiles("test")
@DisplayName("AuthorController Unit Tests")
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Author testAuthor;

    @BeforeEach
    void setUp() throws Exception {
        testAuthor = new Author();
        testAuthor.setId(1L);
        testAuthor.setName("Leo Tolstoy");
        testAuthor.setDescription("Literature classic");

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
    @DisplayName("GET /authors - Should return authors template with paginated data")
    @WithMockUser
    void findAllAuthors_ShouldReturnTemplateWithData() throws Exception {
        Page<Author> authorPage = new PageImpl<>(Collections.singletonList(testAuthor));
        when(authorService.findAllAuthors(any(Pageable.class))).thenReturn(authorPage);

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

        verify(authorService, times(1)).findAllAuthors(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /remove-author/{id} - Should delete author and redirect to authors list")
    @WithMockUser
    void removeAuthor_ShouldDeleteAndRedirect() throws Exception {
        doNothing().when(authorService).deleteAuthor(1L);

        mockMvc.perform(get("/remove-author/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/authors"));

        verify(authorService, times(1)).deleteAuthor(1L);
    }

    @Test
    @DisplayName("GET /update-author/{id} - Should return update template with author data")
    @WithMockUser
    void updateAuthor_ShouldReturnUpdateForm() throws Exception {
        when(authorService.findAuthorById(1L)).thenReturn(testAuthor);

        mockMvc.perform(get("/update-author/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-author"))
                .andExpect(model().attribute("author", testAuthor));

        verify(authorService, times(1)).findAuthorById(1L);
    }

    @Test
    @DisplayName("POST /save-author/{id} - Should update author and redirect when data is valid")
    @WithMockUser
    void updateAuthor_ShouldSaveAndRedirect_WhenValid() throws Exception {
        doNothing().when(authorService).updateAuthor(any(Author.class));

        mockMvc.perform(post("/save-author/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", "1")
                        .param("name", "Anton Chekhov")
                        .param("description", "Famous playwright"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/authors"));

        verify(authorService, times(1)).updateAuthor(any(Author.class));
    }

    @Test
    @DisplayName("POST /save-author/{id} - Should return update template when validation fails")
    @WithMockUser
    void updateAuthor_ShouldReturnUpdateForm_WhenInvalid() throws Exception {
        mockMvc.perform(post("/save-author/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", "1")
                        .param("name", "")
                        .param("description", "Some description"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-author"))
                .andExpect(model().hasErrors());

        verify(authorService, never()).updateAuthor(any(Author.class));
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
    @DisplayName("POST /save-author - Should create author and redirect when data is valid")
    @WithMockUser
    void saveAuthor_ShouldCreateAndRedirect_WhenValid() throws Exception {
        doNothing().when(authorService).createAuthor(any(Author.class));

        mockMvc.perform(post("/save-author")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Fyodor Dostoevsky")
                        .param("description", "Great writer"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/authors"));

        verify(authorService, times(1)).createAuthor(any(Author.class));
    }

    @Test
    @DisplayName("POST /save-author - Should return add-author template when validation fails")
    @WithMockUser
    void saveAuthor_ShouldReturnAddForm_WhenInvalid() throws Exception {
        mockMvc.perform(post("/save-author")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "")
                        .param("description", "Great writer")) // Ошибка валидации
                .andExpect(status().isOk())
                .andExpect(view().name("add-author"))
                .andExpect(model().hasErrors());

        verify(authorService, never()).createAuthor(any(Author.class));
    }
}