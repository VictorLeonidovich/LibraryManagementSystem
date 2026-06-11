package com.kvl.library.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvl.library.controller.BaseWebContainersTest;
import com.kvl.library.dto.PublisherRequestDTO;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("PublisherRestController Integration Tests (PostgreSQL Testcontainers)")
class PublisherRestControllerContainersTest extends BaseWebContainersTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private PublisherRepository publisherRepository;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Publisher savedPublisher;
    private PublisherRequestDTO validRequestDTO;

    @BeforeEach
    void setUp() throws Exception {
        // Базовый класс BaseWebContainersTest автоматически очистит базу перед тестом

        // 1. Сохраняем реальное издательство в PostgreSQL
        Publisher publisher = new Publisher();
        publisher.setName("O'Reilly");
        savedPublisher = publisherRepository.save(publisher);

        // 2. Готовим уникальный DTO для тестов создания
        validRequestDTO = new PublisherRequestDTO();
        validRequestDTO.setName("Manning");

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
    @DisplayName("GET /api/v1/publishers - Should return all publishers paginated")
    @WithMockUser
    void getAllPublishers_WithoutParam_ShouldReturnPaginated() throws Exception {
        mockMvc.perform(get("/api/v1/publishers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(savedPublisher.getId()))
                .andExpect(jsonPath("$.content[0].name").value("O'Reilly"));
    }

    @Test
    @DisplayName("GET /api/v1/publishers?name=... - Should search publishers by name")
    @WithMockUser
    void getAllPublishers_WithParam_ShouldReturnFiltered() throws Exception {
        mockMvc.perform(get("/api/v1/publishers").param("name", "O'Reilly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("O'Reilly"));
    }

    @Test
    @DisplayName("GET /api/v1/publishers/{id} - Should return publisher by id")
    @WithMockUser
    void getPublisherById_ShouldReturnPublisher() throws Exception {
        mockMvc.perform(get("/api/v1/publishers/" + savedPublisher.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPublisher.getId()))
                .andExpect(jsonPath("$.name").value("O'Reilly"));
    }

    @Test
    @DisplayName("GET /api/v1/publishers/{id} - Should return 404 when missing")
    @WithMockUser
    void getPublisherById_NotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/publishers/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/v1/publishers - Should create publisher when user is ADMIN")
    @WithMockUser(roles = "ADMIN")
    void createPublisher_AsAdmin_ShouldReturnCreated() throws Exception {
        mockMvc.perform(post("/api/v1/publishers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Manning"));
    }

    @Test
    @DisplayName("POST /api/v1/publishers - Should return 400 when validation fails")
    @WithMockUser(roles = "ADMIN")
    void createPublisher_InvalidDto_ShouldReturnBadRequest() throws Exception {
        PublisherRequestDTO invalidDto = new PublisherRequestDTO();
        invalidDto.setName(""); // Ошибка валидации: пустое имя нарушает @NotEmpty

        mockMvc.perform(post("/api/v1/publishers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.name").exists());
    }

    @Test
    @DisplayName("POST /api/v1/publishers - Should return 403 when user is not ADMIN")
    @WithMockUser(roles = "USER")
    void createPublisher_AsUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/publishers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isForbidden()); // Spring Security блокирует запрос без тела JSON
    }

    @Test
    @DisplayName("PUT /api/v1/publishers/{id} - Should update publisher when ADMIN")
    @WithMockUser(roles = "ADMIN")
    void updatePublisher_AsAdmin_ShouldReturnOk() throws Exception {
        PublisherRequestDTO updateDto = new PublisherRequestDTO();
        updateDto.setName("O'Reilly - Modified");

        mockMvc.perform(put("/api/v1/publishers/" + savedPublisher.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPublisher.getId()))
                .andExpect(jsonPath("$.name").value("O'Reilly - Modified"));
    }

    @Test
    @DisplayName("DELETE /api/v1/publishers/{id} - Should delete publisher when ADMIN")
    @WithMockUser(roles = "ADMIN")
    void deletePublisher_AsAdmin_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/publishers/" + savedPublisher.getId()).with(csrf()))
                .andExpect(status().isNoContent());

        // Проверяем удаление сквозным образом — повторный GET запрос обязан вернуть 404
        mockMvc.perform(get("/api/v1/publishers/" + savedPublisher.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/publishers/{id} - Should return 403 when user is not ADMIN")
    @WithMockUser(roles = "USER")
    void deletePublisher_AsUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/publishers/" + savedPublisher.getId()).with(csrf()))
                .andExpect(status().isForbidden());

        // Проверяем через репозиторий, что издательство осталось нетронутым в PostgreSQL
        assertThat(publisherRepository.existsById(savedPublisher.getId())).isTrue();
    }
}