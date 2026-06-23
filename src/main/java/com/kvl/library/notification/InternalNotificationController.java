package com.kvl.library.notification;

import com.kvl.library.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

/**
 * Скрытый внутренний REST-контроллер для имитации межсервисного взаимодействия.
 * Принимает Multipart HTTP-запросы и делегирует их выполнение почтовому сервису.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Profile("!test")
@RequestMapping("/api/v1/internal/notifications")
public class InternalNotificationController {

    private final EmailService emailService;

    /**
     * Внутренний эндпоинт для приема Multipart-данных уведомления.
     */
    @PostMapping(value = "/email", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> receiveInternalNotification(
            @RequestPart("to") String to,
            @RequestPart("subject") String subject,
            @RequestPart("text") String text,
            @RequestPart("attachment") byte[] attachment,
            @RequestPart("attachmentName") String attachmentName) {

        log.debug("[REST Receiver] Получен внутренний HTTP-запрос для отправки email на: {}", to);

        // Передаем данные в рабочую имплементацию почты
        emailService.sendEmailWithAttachment(to, subject, text, attachment, attachmentName);

        return ResponseEntity.ok().build();
    }
}