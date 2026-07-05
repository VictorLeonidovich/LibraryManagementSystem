package com.kvl.library.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvl.library.dto.error.ApiErrorResponse;
import com.kvl.library.exception.ApiErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import java.time.LocalDateTime;

/**
 * Главный класс конфигурации безопасности приложения.
 * <p>
 * Настраивает изолированные цепочки фильтров безопасности (SecurityFilterChain)
 * для раздельной поддержки Stateless REST API и Web UI слоев.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Включает поддержку аннотаций @PreAuthorize
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    // Считываем секретный токен для внутренних вызовов из конфигурации приложения
    @Value("${app.security.internal-token:my-super-secret-internal-rpc-token-2026}")
    private String internalToken;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param jwtRequestFilter кастомный JWT фильтр аутентификации
     */
    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    /**
     * ЦЕПОЧКА 1: Защита REST API подсистемы.
     * <p>
     * Перехватывает исключительно запросы, начинающиеся с /api/**. Работает в режиме Stateless.
     */
    @Bean
    @Order(1) // Высокий приоритет: сначала проверяем, не относится ли запрос к REST API
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // Привязываем эту цепочку строго к путям REST API
                .securityMatcher("/api/**")

                // Отключаем CSRF, так как REST API работает на токенах (Stateless)
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))

                .authorizeHttpRequests(auth -> auth
                        // Открытые эндпоинты регистрации и аутентификации
                        .requestMatchers("/api/auth/**").permitAll()

                        // Многоуровневая защита внутренних почтовых RPC-вызовов (Token + IP Allowlist)
                        .requestMatchers("/api/v1/internal/**").access((authentication, context) -> {
                            String requestToken = context.getRequest().getHeader("X-Internal-Token");
                            boolean isTokenValid = internalToken.equals(requestToken);
                            boolean isIpValid = new IpAddressMatcher("127.0.0.1").matches(context.getRequest()) ||
                                    new IpAddressMatcher("::1").matches(context.getRequest());
                            return new AuthorizationDecision(isTokenValid && isIpValid);
                        })

                        // Разграничение прав доступа к бизнес-логике REST API по HTTP-методам
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/**").hasAnyRole("USER", "ADMIN")

                        // Любые другие непредусмотренные REST-запросы требуют токен
                        .anyRequest().authenticated()
                )

                //Подключаем AccessDeniedHandler
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler(apiAccessDeniedHandler())
                )

                // Переводим API сессии в режим STATELESS
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Подключаем JWT фильтр проверки токенов
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * ЦЕПОЧКА 2: Защита Web UI (Thymeleaf), статики и системного мониторинга.
     * <p>
     * Перехватывает все остальные запросы, которые не ушли в REST API.
     */
    @Bean
    @Order(2) // Меньший приоритет: подхватывает всё, что пролетело мимо первой цепочки
    public SecurityFilterChain uiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // Отключаем CSRF для консоли H2 и форм экспорта книг
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/books/*/export"))

                // Разрешаем фреймы для H2-консоли внутри одного домена
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )

                .authorizeHttpRequests(auth -> auth
                        // Статические ресурсы интерфейса Thymeleaf
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        // Техническая документация Swagger UI / OpenAPI
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Встроенная веб-консоль базы данных H2
                        .requestMatchers("/h2-console/**").permitAll()

                        // Метрики Prometheus открыты для сбора (только GET)
                        .requestMatchers(HttpMethod.GET, "/actuator/prometheus").permitAll()
                        // Все остальные критические системные панели Actuator закрываем ролью ADMIN
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // Открытые веб-страницы и формы Thymeleaf UI-контроллеров
                        .requestMatchers("/", "/login", "/error").permitAll()
                        .requestMatchers("/books/**", "/book/**", "/remove-book/**", "/update-book/**", "/save-book/**", "/add-book/**").permitAll()
                        .requestMatchers("/authors/**", "/author/**", "/remove-author/**", "/update-author/**", "/save-author/**", "/add-author/**").permitAll()
                        .requestMatchers("/categories/**", "/category/**", "/remove-category/**", "/update-category/**", "/save-category/**", "/add-category/**").permitAll()
                        .requestMatchers("/publishers/**", "/publisher/**", "/remove-publisher/**", "/update-publisher/**", "/save-publisher/**", "/add-publisher/**").permitAll()

                        // Предохранитель для UI ресурсов
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public AccessDeniedHandler apiAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(ApiErrorCode.ACCESS_DENIED.getHttpStatus().value());
            response.setContentType("application/json;charset=UTF-8");

            ApiErrorResponse errorPayload = ApiErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(ApiErrorCode.ACCESS_DENIED.getHttpStatus().value())
                    .error(ApiErrorCode.ACCESS_DENIED.getHttpStatus().getReasonPhrase())
                    .errorCode(ApiErrorCode.ACCESS_DENIED)
                    .message(ApiErrorCode.ACCESS_DENIED.getDefaultMessage())
                    .path(request.getRequestURI())
                    .build();

            // Пишем наш унифицированный JSON напрямую в HTTP-ответ фильтра
            new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                    .writeValue(response.getWriter(), errorPayload);
        };
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