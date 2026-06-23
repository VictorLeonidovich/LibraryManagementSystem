package com.kvl.library.notification;

/**
 * Интерфейс диспетчера для распределенной отправки уведомлений.
 * Служит фасадом (Pattern Фасад), скрывающим конкретную технологию передачи данных.
 */
public interface NotificationDispatcher {

    /**
     * Маршрутизирует и отправляет email-уведомление с вложенным файлом отчета.
     * Способ передачи определяется активной стратегией в конфигурации приложения.
     *
     * @param to             адрес получателя
     * @param subject        тема письма
     * @param text           текстовое тело письма
     * @param attachment     массив байт вложения (контент файла)
     * @param attachmentName имя прикрепляемого файла
     */
    void dispatchEmail(String to, String subject, String text, byte[] attachment, String attachmentName);
}