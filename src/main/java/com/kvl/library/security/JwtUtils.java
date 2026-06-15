package com.kvl.library.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * Утилитарный компонент для работы с JSON Web Tokens (JWT).
 * <p>
 * Отвечает за генерацию, парсинг, извлечение утверждений (claims) и криптографическую
 * проверку подписи токенов на основе библиотеки JJWT.
 */
@Component
public class JwtUtils {

    // Секретный ключ должен быть длинным (не менее 32 символов)
    @Value("${app.jwt.secret:my-super-safe-and-ultra-long-secret-key-specifically-for-library-system-2026}")
    private String secretKey;

    // Время жизни токена (дефолт: 36000000 мс = 10 часов)
    @Value("${app.jwt.expiration:36000000}")
    private long expirationTime;

    /**
     * Формирует безопасный секретный ключ HMAC-SHA на основе строковой конфигурации.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Генерирует новый JWT-токен для пользователя.
     *
     * @param username имя (логин) пользователя
     * @return строка JWT-токена
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Проверяет валидность токена: совпадение имени и отсутствие истечения срока действия.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}