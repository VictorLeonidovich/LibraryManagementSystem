package com.kvl.library.service;

import com.kvl.library.entity.Book;
import com.kvl.library.enums.ExportFormat;

/**
 * Стратегия генерации отчетов по книге в определенный формат.
 */
public interface BookExportService {

    /**
     * Генерирует документ на основе данных книги.
     */
    byte[] export(Book book);

    /**
     * Возвращает формат, поддерживаемый данной стратегией.
     */
    ExportFormat getSupportedFormat();
}