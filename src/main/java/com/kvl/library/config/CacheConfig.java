package com.kvl.library.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Конфигурационный класс для активации и настройки распределенного кэширования.
 * <p>
 * Аннотация @Profile("prod") гарантирует, что этот класс инициализируется
 * СТРОГО в продакшн-окружении. В тестах (профили test/containers) этот класс
 * полностью игнорируется, предотвращая любые ошибки подключения к Redis.
 */
@Configuration
@EnableCaching
@Profile("prod") // Изолируем кэш Redis от тестовых профилей
public class CacheConfig {

    /**
     * Имя зоны кэша для хранения списка ISBN популярных книг.
     */
    public static final String POPULAR_ISBNS_CACHE = "popularIsbns";

    @Value("${app.cache.ttl-minutes:10}")
    private long cacheTtlMinutes;

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        final GenericJacksonJsonRedisSerializer jsonSerializer = GenericJacksonJsonRedisSerializer.create(builder -> {
            builder.enableSpringCacheNullValueSupport();
        });

        final RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(cacheTtlMinutes))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }
}