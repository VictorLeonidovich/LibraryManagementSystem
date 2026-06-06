package com.kvl.library.controller;

import com.kvl.library.dto.UserLoginDto;
import com.kvl.library.dto.UserRegisterDto;
import com.kvl.library.dto.UserResponseDto;
import com.kvl.library.entity.User;
import com.kvl.library.repository.UserRepository;
import com.kvl.library.security.JwtUtils;
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