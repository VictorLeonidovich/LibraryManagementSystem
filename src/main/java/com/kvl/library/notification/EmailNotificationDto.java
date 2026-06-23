package com.kvl.library.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Data Transfer Object для транспортировки данных почтового уведомления.
 * Спроектирован с учетом изоляции бинарного контента от механизмов логирования.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "attachment") // Исключаем бинарный массив из toString() во избежание утечки памяти в логах
public class EmailNotificationDto {

    private String to;
    private String subject;
    private String text;
    private byte[] attachment;
    private String attachmentName;
}