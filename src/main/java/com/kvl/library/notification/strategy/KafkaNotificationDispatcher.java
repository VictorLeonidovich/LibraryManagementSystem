package com.kvl.library.notification.strategy;

import com.kvl.library.notification.EmailNotificationDto;
import com.kvl.library.notification.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Реализация стратегии асинхронной передачи данных через брокер сообщений Apache Kafka (Паттерн EDA).
 * Служит Producer-компонентом, публикующим события отправки отчетов в распределенный топик.
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.strategy", havingValue = "kafka")
public class KafkaNotificationDispatcher implements NotificationDispatcher {

    private final KafkaTemplate<String, EmailNotificationDto> kafkaTemplate;

    @Value("${app.notification.kafka.topic}")
    private String notificationTopic;

    @Override
    public void dispatchEmail(String to, String subject, String text, byte[] attachment, String attachmentName) {
        log.debug("Активирована брокерская стратегия (Kafka Producer) для адреса: {}", to);

        // Инициализируем чистый транспортный DTO объект для отправки в топик
        EmailNotificationDto payload = new EmailNotificationDto(to, subject, text, attachment, attachmentName);

        // Публикуем сообщение в топик асинхронно с non-blocking non-transactional семантикой
        kafkaTemplate.send(notificationTopic, to, payload)
                .whenComplete((result, exception) -> {
                    if (exception == null) {
                        log.debug("[Kafka Producer] Событие успешно опубликовано в топик '{}'. Offset: {}",
                                notificationTopic, result.getRecordMetadata().offset());
                    } else {
                        log.error("[Kafka Producer] Сбой при публикации события уведомления в брокер Kafka", exception);
                    }
                });
    }
}