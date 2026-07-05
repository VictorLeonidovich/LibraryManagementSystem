package com.kvl.library.service.book.impl;

import com.kvl.library.service.book.BookPopularityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Реализация сервиса ведения метрик популярности на базе Redis.
 * <p>
 * Использует структуру данных Sorted Set для ведения глобального сквозного рейтинга.
 */
@Slf4j
@Service
public class BookPopularityServiceImpl implements BookPopularityService {

    private final StringRedisTemplate redisTemplate;
    private static final String POPULAR_BOOKS_KEY = "library:books:views";

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param redisTemplate шаблон для взаимодействия со структурой данных Redis
     */
    public BookPopularityServiceImpl(@Autowired(required = false) StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void incrementView(String isbn) {
        try {
            if (redisTemplate != null && isbn != null && !isbn.isBlank()) {
                redisTemplate.opsForZSet().incrementScore(POPULAR_BOOKS_KEY, isbn, 1);
            }
        } catch (DataAccessException e) {
            log.warn("Redis infrastructure error while incrementing book view for ISBN '{}': {}", isbn, e.getMessage());
        }
    }

    @Override
    public void removeBook(String isbn) {
        try {
            if (redisTemplate != null && isbn != null && !isbn.isBlank()) {
                redisTemplate.opsForZSet().remove(POPULAR_BOOKS_KEY, isbn);
            }
        } catch (DataAccessException e) {
            log.warn("Redis infrastructure error while removing book with ISBN '{}': {}", isbn, e.getMessage());
        }
    }

    @Override
    public List<String> getTopBooks(int limit) {
        try {
            if (redisTemplate != null) {
                Set<String> typedTupleSet = redisTemplate.opsForZSet().reverseRange(POPULAR_BOOKS_KEY, 0, limit - 1);
                if (typedTupleSet != null && !typedTupleSet.isEmpty()) {
                    return typedTupleSet.stream().toList();
                }
            }
        } catch (DataAccessException e) {
            log.warn("Redis infrastructure error while fetching top {} books: {}", limit, e.getMessage());
        }
        return Collections.emptyList();
    }
}