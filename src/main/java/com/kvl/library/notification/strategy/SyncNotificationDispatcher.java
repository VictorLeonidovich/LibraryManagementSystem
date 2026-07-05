package com.kvl.library.notification.strategy;

import com.kvl.library.notification.NotificationDispatcher;
import com.kvl.library.service.notification.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Реализация стратегии для синхронной передачи данных.
 * Выполняет прямой блокирующий вызов базового почтового сервиса.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
@ConditionalOnProperty(name = "notification.strategy", havingValue = "sync", matchIfMissing = true)
public class SyncNotificationDispatcher implements NotificationDispatcher {

    private final EmailService emailService;

    @Override
    public void dispatchEmail(String to, String subject, String text, byte[] attachment, String attachmentName) {
        log.debug("Активирована синхронная стратегия отправки уведомления на адрес: {}", to);

        // Прямой последовательный вызов в текущем потоке веб-запроса
        emailService.sendEmailWithAttachment(to, subject, text, attachment, attachmentName);
    }
}