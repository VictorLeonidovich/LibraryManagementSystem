package com.kvl.library.service.book;

import com.kvl.library.service.book.impl.BookPopularityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookPopularityService Unit Tests with Mocked Redis")
class BookPopularityServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    private BookPopularityServiceImpl popularityService;

    private final String isbn = "978-5-699-12345-6";
    private final String redisKey = "library:books:views";

    @BeforeEach
    void setUp() {
        // Конфигурируем поведение StringRedisTemplate, чтобы при вызове opsForZSet() он возвращал мок
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        popularityService = new BookPopularityServiceImpl(redisTemplate);
    }

    @Test
    @DisplayName("incrementView() should successfully call Redis incrementScore when ISBN is valid")
    void incrementView_WithValidIsbn_ShouldCallRedis() {
        popularityService.incrementView(isbn);

        // Проверяем, что в Redis ушел инкремент на 1 для нашего ISBN
        verify(zSetOperations, times(1)).incrementScore(redisKey, isbn, 1.0);
    }

    @Test
    @DisplayName("incrementView() should do nothing and skip Redis calls when ISBN is blank")
    void incrementView_WithBlankIsbn_ShouldSkipRedis() {
        popularityService.incrementView("   ");

        verifyNoInteractions(zSetOperations);
    }

    @Test
    @DisplayName("incrementView() should catch exceptions gracefully and log a warning if Redis is down")
    void incrementView_WhenRedisThrowsException_ShouldHandleGracefully() {
        // Симулируем падение Redis
        when(zSetOperations.incrementScore(anyString(), anyString(), anyDouble()))
                .thenThrow(new RuntimeException("Redis connection refused"));

        // Метод не должен выбросить исключение наверх и уронить бизнес-логику приложения
        popularityService.incrementView(isbn);

        verify(zSetOperations, times(1)).incrementScore(redisKey, isbn, 1.0);
    }

    @Test
    @DisplayName("removeBook() should successfully call Redis remove when ISBN is valid")
    void removeBook_WithValidIsbn_ShouldCallRedis() {
        popularityService.removeBook(isbn);

        // Проверяем, что из Redis удалился нужный ISBN
        verify(zSetOperations, times(1)).remove(redisKey, isbn);
    }

    @Test
    @DisplayName("getTopBooks() should return list of ISBNs when Redis has data inside sorted set")
    void getTopBooks_WhenDataExists_ShouldReturnIsbnList() {
        Set<String> mockRedisResult = Set.of(isbn);
        when(zSetOperations.reverseRange(redisKey, 0, 9)).thenReturn(mockRedisResult);

        List<String> result = popularityService.getTopBooks(10);

        assertThat(result).hasSize(1).containsExactly(isbn);
        verify(zSetOperations, times(1)).reverseRange(redisKey, 0, 9);
    }

    @Test
    @DisplayName("getTopBooks() should return empty list when Redis returns empty or null result")
    void getTopBooks_WhenRedisIsEmpty_ShouldReturnEmptyList() {
        when(zSetOperations.reverseRange(redisKey, 0, 9)).thenReturn(Collections.emptySet());

        List<String> result = popularityService.getTopBooks(10);

        assertThat(result).isEmpty();
        verify(zSetOperations, times(1)).reverseRange(redisKey, 0, 9);
    }
}