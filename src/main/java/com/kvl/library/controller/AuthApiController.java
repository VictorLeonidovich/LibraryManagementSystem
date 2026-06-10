package com.kvl.library.controller;

import com.kvl.library.dto.UserLoginDto;
import com.kvl.library.dto.UserRegisterDto;
import com.kvl.library.dto.UserResponseDto;
import com.kvl.library.entity.User;
import com.kvl.library.repository.UserRepository;
import com.kvl.library.security.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Авторизация", description = "Регистрация и аутентификация пользователей (Открытый доступ)")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthApiController(AuthenticationManager authenticationManager,
                             UserDetailsService userDetailsService,
                             JwtUtils jwtUtils,
                             PasswordEncoder passwordEncoder,
                             UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    @Operation(summary = "Аутентификация пользователя и получение JWT-токена",
            description = "Проверяет учетные данные и возвращает строку токена для заголовка Authorization.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный вход в систему",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"token\": \"eyJhbGciOiJIUzI1NiJ9...\"}"))),
            @ApiResponse(responseCode = "400", description = "Некорректный формат запроса"),
            @ApiResponse(responseCode = "401", description = "Неверное имя пользователя или пароль")
    })
    public Map<String, String> login(@Valid @RequestBody UserLoginDto loginDto) {
        // Аутентификация пользователя средствами Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
        );

        // Генерация токена при успешном входе
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginDto.getUsername());
        final String jwt = jwtUtils.generateToken(userDetails.getUsername());

        return Map.of("token", jwt);
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя",
            description = "Создает в системе новую учетную запись с базовой ролью ROLE_USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных или имя пользователя уже занято")
    })
    public UserResponseDto register(@Valid @RequestBody UserRegisterDto registerDto) {
        // Выбрасываем IllegalArgumentException, чтобы ExceptionHandler вернул статус 400 вместо 500
        if (userRepository.findByUsername(registerDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }

        User newUser = new User();
        newUser.setUsername(registerDto.getUsername());
        newUser.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        newUser.setRole("ROLE_USER");

        User savedUser = userRepository.save(newUser);

        return new UserResponseDto(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getRole(),
                "Пользователь успешно зарегистрирован!"
        );
    }
}