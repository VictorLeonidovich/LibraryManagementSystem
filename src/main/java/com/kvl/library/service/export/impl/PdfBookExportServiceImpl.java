package com.kvl.library.service.export.impl;

import com.kvl.library.entity.Book;
import com.kvl.library.entity.Author;
import com.kvl.library.entity.Category;
import com.kvl.library.entity.Publisher;
import com.kvl.library.enums.ExportFormat;
import com.kvl.library.service.export.BookExportService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Реализация сервиса экспорта данных книги в формат PDF.
 * Использует библиотеку OpenPDF для генерации документа с поддержкой кириллицы.
 */
@Slf4j
@Service
public class PdfBookExportServiceImpl implements BookExportService {

    /**
     * Генерирует PDF документ в виде печатной карточки книги.
     *
     * @param book сущность книги
     * @return массив байт, представляющий файл PDF
     * @throws RuntimeException если произошла ошибка при верстке или записи документа
     */
    @Override
    public byte[] export(Book book) {
        log.info("Старт генерации PDF отчета для книги: {}", book.getName());

        // Создаем стандартный документ формата A4 с отступами по 36 пунктов (0.5 дюйма)
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Связываем документ с выходным потоком через PdfWriter
            PdfWriter.getInstance(document, out);
            document.open();

            // --- НАСТРОЙКА КИРИЛЛИЧЕСКОГО ШРИФТА ---
            // Пытаемся найти системный шрифт Arial для корректного отображения русского текста
            String fontPath = "C:/Windows/Fonts/arial.ttf"; // Путь по умолчанию для Windows
            if (!new File(fontPath).exists()) {
                fontPath = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"; // Альтернативный путь для Linux/Docker
            }

            // Если в Docker-контейнере нет шрифтов, OpenPDF переключится на встроенный (русский может не отобразиться)
            BaseFont bf;
            try {
                bf = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } catch (Exception e) {
                log.warn("Системный шрифт не найден по путям. Переключаемся на встроенный Helvetica с кодировкой Cp1251.");
                bf = BaseFont.createFont(BaseFont.HELVETICA, "Cp1251", BaseFont.NOT_EMBEDDED);
            }

            // Создаем стили текста
            Font titleFont = new Font(bf, 18, Font.BOLD, Color.BLACK);
            Font labelFont = new Font(bf, 11, Font.BOLD, Color.BLACK);
            Font valueFont = new Font(bf, 11, Font.NORMAL, Color.DARK_GRAY);

            // --- ЗАГОЛОВОК ДОКУМЕНТА ---
            Paragraph title = new Paragraph("Информационная карточка книги", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // --- СОЗДАНИЕ ТАБЛИЦЫ ---
            // Таблица из 2-х колонок (Характеристика и Значение)
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100); // Растягиваем на всю ширину страницы
            table.setWidths(new float[]{30f, 70f}); // Пропорции колонок (30% и 70%)

            // Добавляем строки в таблицу
            addTableRow(table, "ISBN", book.getIsbn(), labelFont, valueFont);

            // Категории
            String categories = book.getCategories() != null && !book.getCategories().isEmpty()
                    ? book.getCategories().stream().map(Category::getName).collect(Collectors.joining(", "))
                    : "Без категории";
            addTableRow(table, "Категория", categories, labelFont, valueFont);

            // Название книги
            addTableRow(table, "Название книги", book.getName(), labelFont, valueFont);

            // Авторы
            String authors = book.getAuthors() != null && !book.getAuthors().isEmpty()
                    ? book.getAuthors().stream().map(Author::getName).collect(Collectors.joining(", "))
                    : "Автор не указан";
            addTableRow(table, "Автор", authors, labelFont, valueFont);

            // Издатели
            String publishers = book.getPublishers() != null && !book.getPublishers().isEmpty()
                    ? book.getPublishers().stream().map(Publisher::getName).collect(Collectors.joining(", "))
                    : "Издатель не указан";
            addTableRow(table, "Издатель", publishers, labelFont, valueFont);

            // Описание
            addTableRow(table, "Описание", book.getDescription(), labelFont, valueFont);

            // Добавляем готовую таблицу в документ
            document.add(table);

            // Закрываем документ (это фиксирует все изменения)
            document.close();

            log.info("PDF отчет успешно сгенерирован для книги ID: {}", book.getId());
            return out.toByteArray();

        } catch (DocumentException | IOException e) {
            log.error("Критическая ошибка при верстке или записи PDF для книги ID: {}", book.getId(), e);
            throw new RuntimeException("Не удалось сгенерировать PDF отчет", e);
        } finally {
            // Гарантируем закрытие документа, если он остался открыт из-за ошибки в процессе наполнения
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    /**
     * Вспомогательный метод для аккуратного добавления строки в PDF-таблицу.
     */
    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        // Ячейка заголовка (Левая колонка)
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, labelFont));
        cellLabel.setBackgroundColor(new Color(240, 240, 240)); // Легкий серый фон
        cellLabel.setPadding(8);
        cellLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);

        // Ячейка значения (Правая колонка)
        PdfPCell cellValue = new PdfPCell(new Phrase(value != null ? value : "", valueFont));
        cellValue.setPadding(8);
        cellValue.setVerticalAlignment(Element.ALIGN_MIDDLE);

        // Вставляем ячейки поочередно (слева направо)
        table.addCell(cellLabel);
        table.addCell(cellValue);
    }

    @Override
    public ExportFormat getSupportedFormat() {
        return ExportFormat.PDF;
    }
}