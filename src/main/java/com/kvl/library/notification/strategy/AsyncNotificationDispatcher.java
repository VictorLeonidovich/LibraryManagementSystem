package com.kvl.library.notification.strategy;

import com.kvl.library.notification.NotificationDispatcher;
import com.kvl.library.service.notification.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;

/**
 * Реализация стратегии для асинхронной передачи данных.
 * Использует механизмы параллельного выполнения задач Spring Task Execution.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
@ConditionalOnProperty(name = "notification.strategy", havingValue = "async")
public class AsyncNotificationDispatcher implements NotificationDispatcher {

    private final EmailService emailService;

    @Override
    @Async
    public void dispatchEmail(String to, String subject, String text, byte[] attachment, String attachmentName) {
        log.debug("Активирована асинхронная стратегия (@Async) для адреса: {}", to);

        // Оборачиваем выполнение в CompletableFuture для соответствия реактивному стандарту асинхронности
        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendEmailWithAttachment(to, subject, text, attachment, attachmentName);
            } catch (Exception e) {
                log.error("Фоновая отправка email завершилась критической ошибкой", e);
            }
        });
    }
}