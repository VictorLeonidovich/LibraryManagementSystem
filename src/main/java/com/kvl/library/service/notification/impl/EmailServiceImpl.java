package com.kvl.library.service.notification.impl;

import com.kvl.library.service.notification.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Реализация сервиса для отправки уведомлений и отчетов по электронной почте.
 * Интегрируется со Spring Boot Mail стартером.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    /**
     * Отправляет электронное письмо с вложенным файлом отчета.
     *
     * @param to             адрес получателя
     * @param subject        тема письма
     * @param text           текстовое тело письма
     * @param attachment     массив байт вложения
     * @param attachmentName имя прикрепляемого файла (например, "book_report.pdf")
     * @throws RuntimeException если отправка почты или прикрепление файла завершились ошибкой
     */
    @Override
    public void sendEmailWithAttachment(String to, String subject, String text, byte[] attachment, String attachmentName) {
        log.info("Подготовка к отправке email на адрес: {}", to);

        try {
            // Создаем MimeMessage для поддержки вложений (Multipart)
            MimeMessage message = mailSender.createMimeMessage();

            // Флаг true указывает, что письмо будет мультипартовым (с вложениями)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            // Преобразуем массив байт в ресурс Spring для вложения
            ByteArrayResource byteArrayResource = new ByteArrayResource(attachment);

            // Прикрепляем файл к письму
            helper.addAttachment(attachmentName, byteArrayResource);

            // Отправляем сформированное письмо
            mailSender.send(message);
            log.info("Письмо с файлом {} успешно отправлено на адрес: {}", attachmentName, to);

        } catch (MessagingException e) {
            log.error("Ошибка при формировании или отправке email с вложением на адрес: {}", to, e);
            throw new RuntimeException("Не удалось отправить email с отчетом", e);
        }
    }
}