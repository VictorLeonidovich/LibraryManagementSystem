package com.kvl.library.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Изолированная заглушка (Stub) диспетчера уведомлений для тестового окружения.
 * Исключает попытки обращения к брокерам сообщений или сетевым HTTP-портам во время тестов.
 */
@Slf4j
@Component
@Primary // решает конфликт дублирования бинов в контейнерных тестах!
@Profile("test | containers")
public class TestNotificationDispatcher implements NotificationDispatcher {

    @Override
    public void dispatchEmail(String to, String subject, String text, byte[] attachment, String attachmentName) {
        log.info("[TEST STUB] Перехвачен запрос отправки отчета на {}. Инфраструктурные вызовы изолированы.", to);
    }
}