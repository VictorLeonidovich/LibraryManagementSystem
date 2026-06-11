package com.kvl.library.controller.ui;

import com.kvl.library.controller.BaseWebContainersTest;
import com.kvl.library.security.JwtRequestFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@DisplayName("IndexController Thymeleaf Integration Tests (PostgreSQL Testcontainers)")
class IndexControllerContainersTest extends BaseWebContainersTest { // Наследуемся от нашего общего класса

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @BeforeEach
    void setUp() throws Exception {
        // Обучаем публичный метод пропускать запрос сквозь JWT фильтр безопасности
        doAnswer(invocation -> {
            jakarta.servlet.ServletRequest request = invocation.getArgument(0);
            jakarta.servlet.ServletResponse response = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtRequestFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("GET / - Should return index template from full application context")
    @WithMockUser
    void showIndexPage_ShouldReturnIndexTemplate() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }
}