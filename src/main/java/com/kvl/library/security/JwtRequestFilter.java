package com.kvl.library.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Перехватчик (Фильтр) REST API запросов для валидации JWT-токенов.
 * <p>
 * Извлекает заголовок Authorization, проверяет подпись токена и интегрирует
 * аутентифицированного пользователя в контекст безопасности Spring Security.
 */
@Slf4j
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    public JwtRequestFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String username = null;
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            // Именно здесь jjwt выбрасывает исключения, если токен просрочен или подделан.
            username = jwtUtils.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtUtils.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            // Перехватываем просроченный токен и отдаем клиенту JSON 401
            log.warn("Attempt to access with expired JWT token at [{}]: {}", request.getRequestURI(), e.getMessage());
            handleJwtException(response, HttpStatus.UNAUTHORIZED, "JWT token has expired");
        } catch (JwtException e) {
            // Перехватываем невалидную подпись/формат токена и отдаем JSON 400
            log.error("Invalid JWT credentials token anomaly detected at [{}]: {}", request.getRequestURI(), e.getMessage());
            handleJwtException(response, HttpStatus.BAD_REQUEST, "Invalid JWT token signature or format");
        }
    }

    /**
     * Ручное формирование JSON-структуры ошибки в обход Spring MVC ExceptionHandlers.
     */
    // Кастомный метод для отправки JSON ответа клиенту, так как обычный @RestControllerAdvice не умеет ловить ошибки из фильтров.
    private void handleJwtException(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String jsonBody = String.format("{\"status\": %d, \"error\": \"%s\", \"message\": \"%s\"}",
                status.value(), status.getReasonPhrase(), message);

        response.getWriter().write(jsonBody);
    }
}