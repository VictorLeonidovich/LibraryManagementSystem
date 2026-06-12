package com.kvl.library.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("JwtRequestFilter and JwtUtils Unit Coverage Tests")
class JwtRequestFilterTest {

    private JwtUtils jwtUtils;
    private UserDetailsService userDetailsService;
    private JwtRequestFilter jwtRequestFilter;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;
    private StringWriter responseOutput;

    @BeforeEach
    void setUp() throws IOException {
        SecurityContextHolder.clearContext();
        jwtUtils = new JwtUtils();
        // ВРУЧНУЮ ЗАПОЛНЯЕМ @Value ПОЛЯ ЧЕРЕЗ РЕФЛЕКСИЮ ДЛЯ ТЕСТА
        ReflectionTestUtils.setField(jwtUtils, "secretKey", "my-super-safe-and-ultra-long-secret-key-specifically-for-library-system-2026");
        ReflectionTestUtils.setField(jwtUtils, "expirationTime", 36000000L);
        userDetailsService = mock(UserDetailsService.class);
        jwtRequestFilter = new JwtRequestFilter(jwtUtils, userDetailsService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);

        responseOutput = new StringWriter();
        PrintWriter writer = new PrintWriter(responseOutput);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    @DisplayName("JwtUtils - Full Lifecycle Coverage")
    void testJwtUtilsFullCoverage() {
        String username = "testadmin";
        UserDetails userDetails = new User(username, "password", Collections.emptyList());

        String token = jwtUtils.generateToken(username);
        assertThat(token).isNotNull();
        assertThat(jwtUtils.extractUsername(token)).isEqualTo(username);
        assertThat(jwtUtils.validateToken(token, userDetails)).isTrue();
    }

    @Test
    @DisplayName("Filter - Missing Authorization Header")
    void filter_NoHeader_ShouldContinueChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtRequestFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Filter - Invalid Header Format")
    void filter_WrongHeaderPrefix_ShouldContinueChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic abcd123");

        jwtRequestFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Filter - Valid Token Authenticates Successfully")
    void filter_ValidToken_ShouldAuthenticateUser() throws Exception {
        String username = "validuser";
        String token = jwtUtils.generateToken(username);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        UserDetails userDetails = new User(username, "password", Collections.emptyList());
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        jwtRequestFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(username);
    }

    @Test
    @DisplayName("Filter - Handle Expired Token")
    void filter_ExpiredToken_ShouldReturn401Json() throws Exception {
        // Генерируем заведомо просроченный токен вручную для триггера catch-блока
        String expiredToken = Jwts.builder()
                .subject("expireduser")
                .issuedAt(new Date(System.currentTimeMillis() - 20000))
                .expiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(Keys.hmacShaKeyFor("my-super-safe-and-ultra-long-secret-key-specifically-for-library-system-2026".getBytes(StandardCharsets.UTF_8)))
                .compact();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + expiredToken);

        jwtRequestFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        assertThat(responseOutput.toString()).contains("JWT token has expired");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Filter - Handle Malformed Token")
    void filter_MalformedToken_ShouldReturn400Json() throws Exception {
        String malformedToken = "not.a.valid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + malformedToken);

        jwtRequestFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        assertThat(responseOutput.toString()).contains("Invalid JWT token signature or format");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Filter - Skip if user is already authenticated")
    void filter_AlreadyAuthenticated_ShouldSkipValidation() throws Exception {
        String username = "anotheruser";
        String token = jwtUtils.generateToken(username);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        // Имитируем, что пользователь уже вошел в систему до этого фильтра
        org.springframework.security.core.Authentication mockAuth = mock(org.springframework.security.core.Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(mockAuth);

        jwtRequestFilter.doFilter(request, response, filterChain);

        // Фильтр должен просто пропустить запрос дальше, не дергая базу данных
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Filter & Utils - Username mismatch handles false validation")
    void filter_UsernameMismatch_ShouldNotAuthenticate() throws Exception {
        String usernameInToken = "tokenuser";
        String token = jwtUtils.generateToken(usernameInToken);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        // База данных возвращает пользователя с ДРУГИМ именем
        UserDetails dbUser = new org.springframework.security.core.userdetails.User(
                "differentuser", "password", Collections.emptyList()
        );
        when(userDetailsService.loadUserByUsername(usernameInToken)).thenReturn(dbUser);

        jwtRequestFilter.doFilter(request, response, filterChain);

        // Проверяем ветку false в JwtUtils.validateToken и фильтре
        assertThat(jwtUtils.validateToken(token, dbUser)).isFalse();
        verify(filterChain, times(1)).doFilter(request, response);
        // Проверяем, что авторизация НЕ проставилась
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Filter - Handle token with empty or null subject")
    void filter_NullUsernameInToken_ShouldNotAuthenticate() throws Exception {
        // Создаем структурно правильный токен, но без username (subject = null)
        String tokenWithNullSubject = io.jsonwebtoken.Jwts.builder()
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 60000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor("my-super-safe-and-ultra-long-secret-key-specifically-for-library-system-2026".getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + tokenWithNullSubject);

        jwtRequestFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}