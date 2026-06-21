package com.kvl.library.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Главный класс конфигурации безопасности приложения.
 * <p>
 * Настраивает цепочку фильтров безопасности (SecurityFilterChain) для одновременной
 * поддержки Stateless REST API (на основе JWT) и Stateful Web UI (Thymeleaf).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Включает поддержку аннотаций @PreAuthorize
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param jwtRequestFilter кастомный JWT фильтр аутентификации
     */
    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Настройка защиты CSRF
                .csrf(csrf -> csrf
                        // Отключаем CSRF для REST API, консоли H2 и POST-запросов экспорта отчетов
                        .ignoringRequestMatchers("/api/**", "/h2-console/**", "/books/*/export")
                )

                // 2. Настройка заголовков (Разрешаем фреймы для H2-консоли внутри одного домена)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )

                // 3. Настройка прав доступа к URL ресурсам
                .authorizeHttpRequests(auth -> auth
                        // Сначала объявляем все публичные технические эндпоинты обычными строками
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Разрешаем доступ к эндпоинтам мониторинга (Actuator / Prometheus) без авторизации
                        .requestMatchers("/actuator/**").permitAll()

                        // Ограничение прав для REST API бизнес-логики по HTTP методам
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/**").hasAnyRole("USER", "ADMIN")

                        // Все остальные запросы к REST API требуют обязательный токен JWT
                        .requestMatchers("/api/**").authenticated()

                        // Разрешаем свободный доступ к Thymeleaf UI контроллерам и статике (CSS, JS)
                        .anyRequest().permitAll()
                )

                // 4. Переводим сессии управления в режим STATELESS (не сохраняем контекст на сервере)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 5. Интегрируем наш JWT фильтр проверки подлинности перед стандартным фильтром
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}