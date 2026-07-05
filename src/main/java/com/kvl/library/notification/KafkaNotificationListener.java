package com.kvl.library.notification;

import com.kvl.library.service.notification.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumer-компонент архитектуры обмена сообщениями.
 * Осуществляет реактивный мониторинг топика Kafka, извлекает сообщения и инициирует отправку через EmailService.
 */
@Slf4j
@Component
@Profile({"!test", "!containers"})
@RequiredArgsConstructor
public class KafkaNotificationListener {

    private final EmailService emailService;

    /**
     * Потоковый листенер, вычитывающий события пакетной отправки уведомлений.
     * Автоматически интегрирован в пул консьюмеров брокера.
     *
     * @param payload десериализованный объект с контентом и вложением отчета
     */
    @KafkaListener(
            topics = "${app.notification.kafka.topic}",
            groupId = "${spring.kafka.consumer.group-id:library-notification-default-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleNotificationEvent(EmailNotificationDto payload) {
        log.debug("[Kafka Consumer] Перехвачено распределенное событие из брокера для адреса: {}", payload.getTo());

        try {
            // Делегируем финальное исполнение и отправку файла в базовый почтовый сервис
            emailService.sendEmailWithAttachment(
                    payload.getTo(),
                    payload.getSubject(),
                    payload.getText(),
                    payload.getAttachment(),
                    payload.getAttachmentName()
            );
            log.debug("[Kafka Consumer] Обработка события из брокера успешно завершена");
        } catch (Exception e) {
            log.error("[Kafka Consumer] Критическая ошибка при обработке сообщения из топика Kafka", e);
            // В Enterprise здесь настраивается механизм Dead Letter Topic (DLT), но для демо-стадии достаточно лога
        }
    }
}