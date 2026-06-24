package com.kvl.library.notification;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурационный класс для явной инициализации инфраструктуры обмена сообщениями Apache Kafka.
 * Гарантирует создание необходимых компонентов независимо от флагов автоконфигурации Spring.
 */
@Configuration
@Profile({"!test", "!containers"})
public class KafkaNotificationConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:library-notification-default-group}")
    private String groupId;

    @Value("${spring.kafka.producer.key-serializer:org.apache.kafka.common.serialization.StringSerializer}")
    private String keySerializer;

    @Value("${spring.kafka.producer.value-serializer:org.springframework.kafka.support.serializer.JsonSerializer}")
    private String valueSerializer;

    /**
     * Создает фабрику продюсеров с базовыми настройками топологии сети и сериализации.
     */
    @Bean
    public ProducerFactory<String, EmailNotificationDto> notificationProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Объявляет высокоуровневый шаблон KafkaTemplate в контексте приложения.
     */
    @Bean
    public KafkaTemplate<String, EmailNotificationDto> notificationKafkaTemplate() {
        return new KafkaTemplate<>(notificationProducerFactory());
    }

    /**
     * Конфигурация инфраструктуры Consumer (Слушатель сообщений).
     */
    @Bean
    public ConsumerFactory<String, EmailNotificationDto> notificationConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Фабрика контейнеров, адаптированная под Jackson 3 конвертацию по стандартам Spring Boot 4.
     */
    @Bean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, EmailNotificationDto> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EmailNotificationDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(notificationConsumerFactory());

        // 1. Используем специализированный конвертер для текстовых JSON сообщений будущего
        org.springframework.kafka.support.converter.StringJacksonJsonMessageConverter converter =
                new org.springframework.kafka.support.converter.StringJacksonJsonMessageConverter();

        // 2. Настраиваем современный DefaultJacksonJavaTypeMapper под новые стандарты
        org.springframework.kafka.support.mapping.DefaultJacksonJavaTypeMapper typeMapper =
                new org.springframework.kafka.support.mapping.DefaultJacksonJavaTypeMapper();
        typeMapper.addTrustedPackages("com.kvl.library.notification");

        // 3. Передаем маппер напрямую в конвертер по спецификации Spring Kafka 4
        converter.setTypeMapper(typeMapper);

        // 4. Регистрируем готовый конвертер рекордов в фабрике контейнеров
        factory.setRecordMessageConverter(converter);

        return factory;
    }

}