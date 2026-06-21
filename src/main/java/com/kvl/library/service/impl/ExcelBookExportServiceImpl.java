package com.kvl.library.service.impl;

import com.kvl.library.entity.Book;
import com.kvl.library.entity.Author;
import com.kvl.library.entity.Category;
import com.kvl.library.entity.Publisher;
import com.kvl.library.enums.ExportFormat;
import com.kvl.library.service.BookExportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Реализация сервиса экспорта данных книги в формат Excel (XLSX).
 * Использует библиотеку Apache POI для генерации документа.
 */
@Slf4j
@Service
public class ExcelBookExportServiceImpl implements BookExportService {

    /**
     * Генерирует Excel документ со сведениями о книге.
     *
     * @param book сущность книги
     * @return массив байт, представляющий файл XLSX
     * @throws RuntimeException если произошла ошибка при генерации документа
     */
    @Override
    public byte[] export(Book book) {
        log.info("Старт генерации Excel отчета для книги: {}", book.getName());

        // Используем try-with-resources для автоматического закрытия книги (Workbook)
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Создаем лист в книге Excel
            Sheet sheet = workbook.createSheet("Карточка книги");

            // --- НАСТРОЙКА СТИЛЕЙ ---
            // Стиль для жирного шрифта заголовков полей
            CellStyle labelStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            labelStyle.setFont(boldFont);
            labelStyle.setBorderBottom(BorderStyle.THIN);
            labelStyle.setBorderTop(BorderStyle.THIN);
            labelStyle.setBorderLeft(BorderStyle.THIN);
            labelStyle.setBorderRight(BorderStyle.THIN);
            labelStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            labelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Стиль для обычных ячеек с данными
            CellStyle valueStyle = workbook.createCellStyle();
            valueStyle.setBorderBottom(BorderStyle.THIN);
            valueStyle.setBorderTop(BorderStyle.THIN);
            valueStyle.setBorderLeft(BorderStyle.THIN);
            valueStyle.setBorderRight(BorderStyle.THIN);
            valueStyle.setWrapText(true); // Разрешаем перенос текста для длинных описаний

            // --- ЗАПОЛНЕНИЕ ДАННЫХ ---
            // Шапка таблицы (Название метаданных и Значение)
            Row headerRow = sheet.createRow(0);
            createCell(headerRow, 0, "Характеристика", labelStyle);
            createCell(headerRow, 1, "Значение", labelStyle);

            // 1. ISBN
            Row isbnRow = sheet.createRow(1);
            createCell(isbnRow, 0, "ISBN", labelStyle);
            createCell(isbnRow, 1, book.getIsbn(), valueStyle);

            // 2. Категории (собираем через запятую)
            Row categoryRow = sheet.createRow(2);
            createCell(categoryRow, 0, "Категория", labelStyle);
            String categories = book.getCategories() != null && !book.getCategories().isEmpty()
                    ? book.getCategories().stream().map(Category::getName).collect(Collectors.joining(", "))
                    : "Без категории";
            createCell(categoryRow, 1, categories, valueStyle);

            // 3. Название книги
            Row nameRow = sheet.createRow(3);
            createCell(nameRow, 0, "Название книги", labelStyle);
            createCell(nameRow, 1, book.getName(), valueStyle);

            // 4. Авторы (собираем через запятую)
            Row authorRow = sheet.createRow(4);
            createCell(authorRow, 0, "Автор", labelStyle);
            String authors = book.getAuthors() != null && !book.getAuthors().isEmpty()
                    ? book.getAuthors().stream().map(Author::getName).collect(Collectors.joining(", "))
                    : "Автор не указан";
            createCell(authorRow, 1, authors, valueStyle);

            // 5. Издатели (собираем через запятую)
            Row publisherRow = sheet.createRow(5);
            createCell(publisherRow, 0, "Издатель", labelStyle);
            String publishers = book.getPublishers() != null && !book.getPublishers().isEmpty()
                    ? book.getPublishers().stream().map(Publisher::getName).collect(Collectors.joining(", "))
                    : "Издатель не указан";
            createCell(publisherRow, 1, publishers, valueStyle);

            // 6. Описание книги
            Row descRow = sheet.createRow(6);
            createCell(descRow, 0, "Описание", labelStyle);
            createCell(descRow, 1, book.getDescription(), valueStyle);

            // Автоматическое выравнивание ширины колонок под контент
            sheet.autoSizeColumn(0);
            sheet.setColumnWidth(1, 15000); // Для описания зафиксируем ширину, так как включен перенос текста

            // Записываем документ в поток байт
            workbook.write(out);
            log.info("Excel отчет успешно сгенерирован для книги ID: {}", book.getId());
            return out.toByteArray();

        } catch (IOException e) {
            log.error("Критическая ошибка ввода-вывода при генерации Excel для книги ID: {}", book.getId(), e);
            throw new RuntimeException("Не удалось сгенерировать Excel отчет", e);
        }
    }

    /**
     * Вспомогательный метод для создания ячейки со стилем.
     */
    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    @Override
    public ExportFormat getSupportedFormat() {
        return ExportFormat.XLSX;
    }
}