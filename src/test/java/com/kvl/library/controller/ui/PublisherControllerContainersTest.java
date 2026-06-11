package com.kvl.library.controller.ui;

import com.kvl.library.controller.BaseWebContainersTest;
import com.kvl.library.entity.Publisher;
import com.kvl.library.repository.PublisherRepository;
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

@DisplayName("PublisherController Thymeleaf Integration Tests (PostgreSQL Testcontainers)")
class PublisherControllerContainersTest extends BaseWebContainersTest {

    @Autowired
    private PublisherRepository publisherRepository;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Publisher savedPublisher;

    @BeforeEach
    void setUp() throws Exception {
        // Сохраняем реальное издательство в PostgreSQL
        Publisher publisher = new Publisher();
        publisher.setName("O'Reilly");
        savedPublisher = publisherRepository.save(publisher);

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
    @DisplayName("GET /publishers - Should return publishers template with paginated data from PostgreSQL")
    @WithMockUser
    void findAllPublishers_ShouldReturnTemplateWithData() throws Exception {
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
    }

    @Test
    @DisplayName("GET /remove-publisher/{id} - Should delete publisher from PostgreSQL and redirect")
    @WithMockUser
    void removePublisher_ShouldDeleteAndRedirect() throws Exception {
        mockMvc.perform(get("/remove-publisher/" + savedPublisher.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/publishers"));

        // Проверяем физическое удаление из PostgreSQL контейнера
        assertThat(publisherRepository.existsById(savedPublisher.getId())).isFalse();
    }

    @Test
    @DisplayName("GET /update-publisher/{id} - Should return update template with publisher data from PostgreSQL")
    @WithMockUser
    void updatePublisher_ShouldReturnUpdateForm() throws Exception {
        mockMvc.perform(get("/update-publisher/" + savedPublisher.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("update-publisher"))
                .andExpect(model().attributeExists("publisher"));
    }

    @Test
    @DisplayName("POST /save-publisher/{id} - Should update publisher in PostgreSQL and redirect when data is valid")
    @WithMockUser
    void updatePublisher_ShouldSaveAndRedirect_WhenValid() throws Exception {
        mockMvc.perform(post("/save-publisher/" + savedPublisher.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", savedPublisher.getId().toString())
                        .param("name", "Manning"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/publishers"));

        // Проверяем, что в базе PostgreSQL имя действительно обновилось
        Publisher updatedPublisher = publisherRepository.findById(savedPublisher.getId()).orElseThrow();
        assertThat(updatedPublisher.getName()).isEqualTo("Manning");
    }

    @Test
    @DisplayName("POST /save-publisher/{id} - Should return update template when validation fails")
    @WithMockUser
    void updatePublisher_ShouldReturnUpdateForm_WhenInvalid() throws Exception {
        mockMvc.perform(post("/save-publisher/" + savedPublisher.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", savedPublisher.getId().toString())
                        .param("name", "")) // Пустая строка триггерит Jakarta Validation
                .andExpect(status().isOk())
                .andExpect(view().name("update-publisher"))
                .andExpect(model().hasErrors());
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
    @DisplayName("POST /save-publisher - Should create publisher in PostgreSQL and redirect when data is valid")
    @WithMockUser
    void savePublisher_ShouldCreateAndRedirect_WhenValid() throws Exception {
        mockMvc.perform(post("/save-publisher")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Packt"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/publishers"));
    }

    @Test
    @DisplayName("POST /save-publisher - Should return add-publisher template when validation fails")
    @WithMockUser
    void savePublisher_ShouldReturnAddForm_WhenInvalid() throws Exception {
        mockMvc.perform(post("/save-publisher")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "")) // Нарушает @NotEmpty
                .andExpect(status().isOk())
                .andExpect(view().name("add-publisher"))
                .andExpect(model().hasErrors());
    }
}