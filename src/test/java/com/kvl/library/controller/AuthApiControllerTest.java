package com.kvl.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvl.library.controller.api.AuthApiController;
import com.kvl.library.dto.auth.UserLoginDto;
import com.kvl.library.dto.auth.UserRegisterDto;
import com.kvl.library.entity.User;
import com.kvl.library.repository.UserRepository;
import com.kvl.library.security.JwtRequestFilter;
import com.kvl.library.security.JwtUtils;
import com.kvl.library.exception.ApiGlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthApiController.class)
@Import(ApiGlobalExceptionHandler.class)
@ActiveProfiles("test")
@DisplayName("AuthApiController Unit Tests")
class AuthApiControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    // Временная конфигурация безопасности, которая полностью отключает проверки для этого теста
    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            jakarta.servlet.ServletRequest request = invocation.getArgument(0);
            jakarta.servlet.ServletResponse response = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtRequestFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return JWT token when credentials are valid")
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("user");
        loginDto.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("user", "password"));

        UserDetails mockUserDetails = mock(UserDetails.class);
        when(mockUserDetails.getUsername()).thenReturn("user");
        when(userDetailsService.loadUserByUsername("user")).thenReturn(mockUserDetails);
        when(jwtUtils.generateToken("user")).thenReturn("mocked-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 401 Unauthorized when credentials are invalid")
    void login_WithInvalidCredentials_ShouldFail() throws Exception {
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setUsername("user");
        loginDto.setPassword("wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

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

        User mockSavedUser = new User();
        mockSavedUser.setId(1L);
        mockSavedUser.setUsername("newuser");
        mockSavedUser.setRole("ROLE_USER");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("securePass")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockSavedUser);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.message").value("Пользователь успешно зарегистрирован!"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 Bad Request when user already exists")
    void register_ExistingUser_ShouldReturnBusinessError() throws Exception {
        UserRegisterDto registerDto = new UserRegisterDto();
        registerDto.setUsername("existing");
        registerDto.setPassword("password123");

        when(userRepository.findByUsername("existing")).thenReturn(Optional.of(new User()));

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
        registerDto.setUsername("usr");
        registerDto.setPassword("123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors.username").value("Имя пользователя должно быть от 4 до 20 символов"))
                .andExpect(jsonPath("$.validationErrors.password").value("Пароль должен содержать минимум 6 символов"));
    }
}