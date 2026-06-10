package com.kvl.library.controller;

import com.kvl.library.controller.ui.PublisherController;
import com.kvl.library.entity.Publisher;
import com.kvl.library.security.JwtRequestFilter;
import com.kvl.library.service.PublisherService;
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

@WebMvcTest(PublisherController.class)
@ActiveProfiles("test")
@DisplayName("PublisherController Unit Tests")
class PublisherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublisherService publisherService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Publisher testPublisher;

    @BeforeEach
    void setUp() throws Exception {
        testPublisher = new Publisher();
        testPublisher.setId(1L);
        testPublisher.setName("O'Reilly");

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
    @DisplayName("GET /publishers - Should return publishers template with paginated data")
    @WithMockUser
    void findAllPublishers_ShouldReturnTemplateWithData() throws Exception {
        Page<Publisher> publisherPage = new PageImpl<>(Collections.singletonList(testPublisher));
        when(publisherService.findAllPublishers(any(Pageable.class))).thenReturn(publisherPage);

        mockMvc.perform(get("/publishers")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("publishers"))
                .andExpect(model().attributeExists("publishers"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("totalItems", 1L))
                .andExpect(model().attribute("size", 5));

        verify(publisherService, times(1)).findAllPublishers(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /remove-publisher/{id} - Should delete publisher and redirect to publishers list")
    @WithMockUser
    void removePublisher_ShouldDeleteAndRedirect() throws Exception {
        doNothing().when(publisherService).deletePublisher(1L);

        mockMvc.perform(get("/remove-publisher/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/publishers"));

        verify(publisherService, times(1)).deletePublisher(1L);
    }

    @Test
    @DisplayName("GET /update-publisher/{id} - Should return update template with publisher data")
    @WithMockUser
    void updatePublisher_ShouldReturnUpdateForm() throws Exception {
        when(publisherService.findPublisherById(1L)).thenReturn(testPublisher);

        mockMvc.perform(get("/update-publisher/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("update-publisher"))
                .andExpect(model().attribute("publisher", testPublisher));

        verify(publisherService, times(1)).findPublisherById(1L);
    }

    @Test
    @DisplayName("POST /save-publisher/{id} - Should update publisher and redirect when data is valid")
    @WithMockUser
    void updatePublisher_ShouldSaveAndRedirect_WhenValid() throws Exception {
        doNothing().when(publisherService).updatePublisher(any(Publisher.class));

        mockMvc.perform(post("/save-publisher/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", "1")
                        .param("name", "Manning"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/publishers"));

        verify(publisherService, times(1)).updatePublisher(any(Publisher.class));
    }

    @Test
    @DisplayName("POST /save-publisher/{id} - Should return update template when validation fails")
    @WithMockUser
    void updatePublisher_ShouldReturnUpdateForm_WhenInvalid() throws Exception {
        mockMvc.perform(post("/save-publisher/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", "1")
                        .param("name", "")) // Пустое имя триггерит BindingResult ошибки
                .andExpect(status().isOk())
                .andExpect(view().name("update-publisher"))
                .andExpect(model().hasErrors());

        verify(publisherService, never()).updatePublisher(any(Publisher.class));
    }

    @Test
    @DisplayName("GET /add-publisher - Should return add-publisher template with empty publisher object")
    @WithMockUser
    void addPublisher_ShouldReturnAddForm() throws Exception {
        mockMvc.perform(get("/add-publisher"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-publisher"))
                .andExpect(model().attributeExists("publisher"));
    }

    @Test
    @DisplayName("POST /save-publisher - Should create publisher and redirect when data is valid")
    @WithMockUser
    void savePublisher_ShouldCreateAndRedirect_WhenValid() throws Exception {
        doNothing().when(publisherService).createPublisher(any(Publisher.class));

        mockMvc.perform(post("/save-publisher")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Packt"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/publishers"));

        verify(publisherService, times(1)).createPublisher(any(Publisher.class));
    }

    @Test
    @DisplayName("POST /save-publisher - Should return add-publisher template when validation fails")
    @WithMockUser
    void savePublisher_ShouldReturnAddForm_WhenInvalid() throws Exception {
        mockMvc.perform(post("/save-publisher")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "")) // Ошибка валидации
                .andExpect(status().isOk())
                .andExpect(view().name("add-publisher"))
                .andExpect(model().hasErrors());

        verify(publisherService, never()).createPublisher(any(Publisher.class));
    }
}