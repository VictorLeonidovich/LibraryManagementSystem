package com.kvl.library.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Включает поддержку аннотаций @PreAuthorize
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                // Отключаем CSRF для REST API, так как токены защищены от CSRF-атак
                .csrf(AbstractHttpConfigurer::disable)

                // Настройка прав доступа к URL
                .authorizeHttpRequests(auth -> auth
                        // 1. Открытый доступ для авторизации и документации Swagger
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // 2. Ограничение по ролям на уровне URL (Важно: без префикса ROLE_)
                        // Разрешаем удаление, создание и обновление только для ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/**").hasRole("ADMIN")

                        // Чтение данных (GET) доступно и USER, и ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/v1/**").hasAnyRole("USER", "ADMIN")

                        // 3. Все остальные запросы к API должны быть просто аутентифицированы
                        .requestMatchers("/api/**").authenticated()    // Все остальные REST API требуют JWT
                        .requestMatchers("/h2-console/**").permitAll() // Разрешаем доступ к консоли H2
                        .anyRequest().permitAll()                        // Разрешаем доступ к Thymeleaf страницам
                )

                // Разрешаем отображение интерфейса H2 в frame-структурах
                //.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                // Для REST API отключаем хранение сессий на сервере
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Добавляем наш JWT фильтр перед стандартным UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
