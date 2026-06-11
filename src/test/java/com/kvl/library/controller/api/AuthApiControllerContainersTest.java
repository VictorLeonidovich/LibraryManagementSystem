package com.kvl.library.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvl.library.controller.BaseWebContainersTest;
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

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AuthApiController Integration Tests (PostgreSQL Testcontainers)")
class AuthApiControllerContainersTest extends BaseWebContainersTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Используем настоящий бин кодирования паролей

    private User existingUser;

    @BeforeEach
    void setUp() {
        // Создаем и сохраняем реального пользователя с хэшированным паролем в PostgreSQL
        User user = new User();
        user.setUsername("existing_user");
        user.setPassword(passwordEncoder.encode("password123")); // Честный хэш BCrypt
        user.setRole("ROLE_USER");
        existingUser = userRepository.save(user);
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return REAL JWT token when credentials are valid")
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("existing_user");
        loginDto.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                // Проверяем, что система сгенерировала настоящий, не пустой JWT-токен
                .andExpect(jsonPath("$.token").value(notNullValue()));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 401 Unauthorized when credentials are invalid")
    void login_WithInvalidCredentials_ShouldFail() throws Exception {
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("existing_user");
        loginDto.setPassword("wrong_password"); // Неверный пароль

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Неверное имя пользователя или пароль"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 400 when username or password is blank")
    void login_WithBlankFields_ShouldReturnValidationError() throws Exception {
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("");
        loginDto.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors.username").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register - Should register new user successfully and return UserResponseDto")
    void register_NewUser_ShouldReturnUserResponseDto() throws Exception {
        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setUsername("newuser");
        registerDto.setPassword("securePass");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                // Проверяем, что PostgreSQL сгенерировал реальный автоинкрементный ID
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.message").value("Пользователь успешно зарегистрирован!"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 Bad Request when user already exists in PostgreSQL")
    void register_ExistingUser_ShouldReturnBusinessError() throws Exception {
        UserRegisterDto registerDto = new UserRegisterDto();
        // Используем логин "existing_user", который уже сохранили в базу в методе setUp()
        registerDto.setUsername("existing_user");
        registerDto.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Пользователь с таким именем уже существует"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 when registration data fails validation rules")
    void register_InvalidData_ShouldReturnValidationErrors() throws Exception {
        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setUsername("usr"); // Слишком короткое имя
        registerDto.setPassword("123"); // Слишком короткий пароль

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors.username").value("Имя пользователя должно быть от 4 до 20 символов"))
                .andExpect(jsonPath("$.validationErrors.password").value("Пароль должен содержать минимум 6 символов"));
    }
}