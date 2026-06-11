package com.kvl.library.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvl.library.dto.PublisherRequestDTO;
import com.kvl.library.dto.PublisherResponseDTO;
import com.kvl.library.entity.Publisher;
import com.kvl.library.exception.EntityNotFoundException;
import com.kvl.library.mapper.PublisherMapper;
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

@WebMvcTest(PublisherRestController.class)
@ActiveProfiles("test")
@EnableMethodSecurity
@DisplayName("PublisherRestController Unit Tests")
class PublisherRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PublisherService publisherService;

    @MockitoBean
    private PublisherMapper publisherMapper;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Publisher testPublisher;
    private PublisherResponseDTO testResponseDTO;
    private PublisherRequestDTO validRequestDTO;

    @BeforeEach
    void setUp() throws Exception {
        testPublisher = new Publisher();
        testPublisher.setId(1L);
        testPublisher.setName("O'Reilly");

        testResponseDTO = new PublisherResponseDTO();
        testResponseDTO.setId(1L);
        testResponseDTO.setName("O'Reilly");

        validRequestDTO = new PublisherRequestDTO();
        validRequestDTO.setName("O'Reilly");

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
        Page<Publisher> page = new PageImpl<>(Collections.singletonList(testPublisher));
        when(publisherService.findAllPublishers(any(Pageable.class))).thenReturn(page);
        when(publisherMapper.toResponseDTO(testPublisher)).thenReturn(testResponseDTO);

        mockMvc.perform(get("/api/v1/publishers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("O'Reilly"));
    }

    @Test
    @DisplayName("GET /api/v1/publishers?name=... - Should search publishers by name")
    @WithMockUser
    void getAllPublishers_WithParam_ShouldReturnFiltered() throws Exception {
        Page<Publisher> page = new PageImpl<>(Collections.singletonList(testPublisher));
        when(publisherService.searchPublishersByName(eq("O'Reilly"), any(Pageable.class))).thenReturn(page);
        when(publisherMapper.toResponseDTO(testPublisher)).thenReturn(testResponseDTO);

        mockMvc.perform(get("/api/v1/publishers").param("name", "O'Reilly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("O'Reilly"));
    }

    @Test
    @DisplayName("GET /api/v1/publishers/{id} - Should return publisher by id")
    @WithMockUser
    void getPublisherById_ShouldReturnPublisher() throws Exception {
        when(publisherService.findPublisherById(1L)).thenReturn(testPublisher);
        when(publisherMapper.toResponseDTO(testPublisher)).thenReturn(testResponseDTO);

        mockMvc.perform(get("/api/v1/publishers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("O'Reilly"));
    }

    @Test
    @DisplayName("GET /api/v1/publishers/{id} - Should return 404 when missing")
    @WithMockUser
    void getPublisherById_NotFound_ShouldReturn404() throws Exception {
        when(publisherService.findPublisherById(99L)).thenThrow(new EntityNotFoundException("Publisher not found"));

        mockMvc.perform(get("/api/v1/publishers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/v1/publishers - Should create publisher when user is ADMIN")
    @WithMockUser(roles = "ADMIN")
    void createPublisher_AsAdmin_ShouldReturnCreated() throws Exception {
        when(publisherMapper.toEntity(any(PublisherRequestDTO.class))).thenReturn(testPublisher);
        doNothing().when(publisherService).createPublisher(any(Publisher.class));
        when(publisherMapper.toResponseDTO(testPublisher)).thenReturn(testResponseDTO);

        mockMvc.perform(post("/api/v1/publishers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("POST /api/v1/publishers - Should return 400 when validation fails")
    @WithMockUser(roles = "ADMIN")
    void createPublisher_InvalidDto_ShouldReturnBadRequest() throws Exception {
        PublisherRequestDTO invalidDto = new PublisherRequestDTO();
        invalidDto.setName("");

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
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("PUT /api/v1/publishers/{id} - Should update publisher when ADMIN")
    @WithMockUser(roles = "ADMIN")
    void updatePublisher_AsAdmin_ShouldReturnOk() throws Exception {
        when(publisherService.findPublisherById(1L)).thenReturn(testPublisher);
        doNothing().when(publisherMapper).updateEntityFromDto(any(PublisherRequestDTO.class), any(Publisher.class));
        doNothing().when(publisherService).updatePublisher(any(Publisher.class));
        when(publisherMapper.toResponseDTO(testPublisher)).thenReturn(testResponseDTO);

        mockMvc.perform(put("/api/v1/publishers/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/publishers/{id} - Should delete publisher when ADMIN")
    @WithMockUser(roles = "ADMIN")
    void deletePublisher_AsAdmin_ShouldReturnNoContent() throws Exception {
        doNothing().when(publisherService).deletePublisher(1L);

        mockMvc.perform(delete("/api/v1/publishers/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/publishers/{id} - Should return 403 when user is not ADMIN")
    @WithMockUser(roles = "USER")
    void deletePublisher_AsUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/publishers/1").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}