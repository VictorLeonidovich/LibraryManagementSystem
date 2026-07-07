package com.kvl.library;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvl.library.controller.BaseWebContainersTest;
import com.kvl.library.dto.BookRequestDTO;
import com.kvl.library.dto.auth.UserLoginDto;
import com.kvl.library.dto.auth.UserRegisterDto;
import com.kvl.library.entity.User;
import com.kvl.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("LibraryApplication Full-Stack Smoke Integration Tests with Testcontainers")
class LibrarySmokeIntegrationTest extends BaseWebContainersTest { // Наследуем веб-инициализатор

    // Создаем ObjectMapper вручную, так как в RANDOM_PORT режиме контекст его не внедряет автоматически
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // Очищаем таблицу пользователей. Справочники очистит BaseWebContainersTest в @AfterEach
        userRepository.deleteAll();

        // 1. Создаем тестового администратора для обхода проверок @PreAuthorize("hasRole('ADMIN')")
        User admin = new User();
        admin.setUsername("admin_smoke");
        admin.setPassword(passwordEncoder.encode("admin_pass"));
        admin.setRole("ROLE_ADMIN");
        userRepository.save(admin);

        // 2. Создаем тестового обычного пользователя
        User user = new User();
        user.setUsername("user_smoke");
        user.setPassword(passwordEncoder.encode("user_pass"));
        user.setRole("ROLE_USER");
        userRepository.save(user);

        // Извлекаем легитимные JWT-токены через реальный эндпоинт авторизации подсистемы безопасности
        adminToken = obtainToken("admin_smoke", "admin_pass");
        userToken = obtainToken("user_smoke", "user_pass");
    }

    private String obtainToken(String username, String password) throws Exception {
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername(username);
        loginDto.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        String responseStr = result.getResponse().getContentAsString();
        Map<?, ?> map = objectMapper.readValue(responseStr, Map.class);
        return "Bearer " + map.get("token");
    }

    @Test
    @DisplayName("Full application smoke cycle should execute successfully when valid and invalid requests are sent")
    void shouldExecuteFullApplicationCycleWhenValidAndInvalidRequestsAreSent() throws Exception {

        // --- 1. ТЕСТ РЕГИСТРАЦИИ ПОЛЬЗОВАТЕЛЯ ---
        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setUsername("new_user");
        registerDto.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("new_user")))
                .andExpect(jsonPath("$.role", is("ROLE_USER")));

        // --- 2. ПРОВЕРКА ВАЛИДАЦИИ ДАННЫХ В КОНТРОЛЛЕРЕ (Невалидные поля DTO) ---
        BookRequestDTO invalidBook = new BookRequestDTO();
        invalidBook.setName("B"); // Ошибка: Минимальная длина должна быть от 2 символов
        invalidBook.setIsbn("");  // Ошибка: Поле не должно быть пустым
        invalidBook.setDescription("Short");

        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest()); // Ожидаем 400 Bad Request от валидатора Jakarta

        // --- 3. ПРОВЕРКА ОГРАНИЧЕНИЯ РОЛЕЙ (USER пытается вызвать методы ADMIN) ---
        BookRequestDTO validBook = new BookRequestDTO();
        validBook.setName("Smoke Test Book");
        validBook.setIsbn("111-222-333");
        validBook.setDescription("Valid integration description");
        validBook.setAuthorIds(Set.of());
        validBook.setCategoryIds(Set.of());
        validBook.setPublisherIds(Set.of());

        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", userToken) // Передаем токен обычного пользователя
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBook)))
                .andExpect(status().isForbidden()); // Ожидаем 403 Forbidden от правил Spring Security

        // --- 4. УСПЕШНЫЙ CRUD ЦИКЛ СУЩНОСТИ (POST -> GET -> PUT -> DELETE) ---

        // Шаг А: Создание записи в СУБД (POST)
        MvcResult createResult = mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBook)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Smoke Test Book")))
                .andReturn();

        String responseStr = createResult.getResponse().getContentAsString();
        Map<?, ?> createdBook = objectMapper.readValue(responseStr, Map.class);
        Integer bookId = (Integer) createdBook.get("id");

        // Шаг Б: Чтение созданной записи (GET) - проверка работы логики без Redis (spring.cache.type=none)
        mockMvc.perform(get("/api/v1/books/" + bookId)
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookId)))
                .andExpect(jsonPath("$.isbn", is("111-222-333")));

        // Шаг В: Обновление записи (PUT)
        validBook.setName("Updated Smoke Book");
        mockMvc.perform(put("/api/v1/books/" + bookId)
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validBook)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Smoke Book")));

        // Шаг Г: Удаление записи из СУБД (DELETE)
        mockMvc.perform(delete("/api/v1/books/" + bookId)
                        .header("Authorization", adminToken))
                .andExpect(status().isNoContent());

        // Шаг Д: Проверка физического отсутствия записи в PostgreSQL после удаления
        mockMvc.perform(get("/api/v1/books/" + bookId)
                        .header("Authorization", userToken))
                .andExpect(status().isNotFound());
    }
}