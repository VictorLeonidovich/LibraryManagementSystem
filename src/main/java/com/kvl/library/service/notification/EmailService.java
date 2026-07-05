package com.kvl.library.service.notification;

/**
 * Сервис для отправки уведомлений по электронной почте.
 */
public interface EmailService {

    /**
     * Отправляет email с вложенным файлом отчета.
     */
    void sendEmailWithAttachment(String to, String subject, String text, byte[] attachment, String attachmentName);
}