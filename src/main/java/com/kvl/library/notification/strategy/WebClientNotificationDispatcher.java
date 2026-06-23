package com.kvl.library.notification.strategy;

import com.kvl.library.notification.NotificationDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Реализация стратегии межсервисного взаимодействия через реактивный неблокирующий WebClient.
 * Имитирует сетевую отправку данных на удаленный микросервис уведомлений.
 */
@Slf4j
@Component
@Profile("!test")
@ConditionalOnProperty(name = "notification.strategy", havingValue = "webclient")
public class WebClientNotificationDispatcher implements NotificationDispatcher {

    private final WebClient webClient;

    /**
     * Конструктор инициализирует WebClient, настроенный на локальный хост приложения.
     */
    public WebClientNotificationDispatcher() {
        // Настраиваем базовый URL на корень запущенного приложения
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8080")
                .build();
    }

    @Override
    public void dispatchEmail(String to, String subject, String text, byte[] attachment, String attachmentName) {
        log.debug("Активирована реактивная стратегия (WebClient) для отправки уведомления на: {}", to);

        // Формируем Multipart Body, аналогично стандарту передачи файлов в Enterprise
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("to", to);
        bodyBuilder.part("subject", subject);
        bodyBuilder.part("text", text);
        bodyBuilder.part("attachment", attachment)
                .filename(attachmentName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM);
        bodyBuilder.part("attachmentName", attachmentName);

        // Выполняем асинхронный неблокирующий POST-запрос в реактивном стиле
        webClient.post()
                .uri("/api/v1/internal/notifications/email")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .toBodilessEntity()
                // .subscribe() запускает выполнение в фоновом реактивном потоке (Fire and Forget)
                .subscribe(
                        response -> log.debug("[WebClient] Уведомление успешно доставлено на внутренний REST-эндпоинт"),
                        error -> log.error("[WebClient] Критическая ошибка при сетевой передаче уведомления", error)
                );
    }
}