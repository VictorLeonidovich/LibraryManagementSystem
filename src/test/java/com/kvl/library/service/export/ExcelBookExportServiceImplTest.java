package com.kvl.library.service.export;

import com.kvl.library.entity.Author;
import com.kvl.library.entity.Book;
import com.kvl.library.entity.Category;
import com.kvl.library.entity.Publisher;
import com.kvl.library.enums.ExportFormat;
import com.kvl.library.service.export.impl.ExcelBookExportServiceImpl;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExcelBookExportServiceImpl Unit Tests")
class ExcelBookExportServiceImplTest {

    @InjectMocks
    private ExcelBookExportServiceImpl excelBookExportService;

    private Book fullBook;
    private Book emptyBook;

    @BeforeEach
    void setUp() {
        // 1. Настраиваем заполненную книгу (для проверки успешного пути и склеивания списков)
        Category category = new Category();
        category.setName("Фантастика");

        Author author1 = new Author();
        author1.setName("Айзек Азимов");
        Author author2 = new Author();
        author2.setName("Роберт Хайнлайн");

        Publisher publisher = new Publisher();
        publisher.setName("Эксмо");

        fullBook = new Book();
        fullBook.setId(1L);
        fullBook.setIsbn("978-5-699-12345-6");
        fullBook.setName("Основание");
        fullBook.setDescription("Культурное наследие фантастики.");
        fullBook.setCategories(Set.of(category));
        fullBook.setAuthors(Set.of(author1, author2));
        fullBook.setPublishers(Set.of(publisher));

        // 2. Настраиваем пустую книгу (для покрытия веток if/else с "Без категории", "Автор не указан" и т.д.)
        emptyBook = new Book();
        emptyBook.setId(2L);
        emptyBook.setIsbn(null);
        emptyBook.setName("Пустая книга");
        emptyBook.setDescription(null);
        emptyBook.setCategories(Collections.emptySet());
        emptyBook.setAuthors(null); // Проверим одновременно и null, и emptySet
        emptyBook.setPublishers(Collections.emptySet());
    }

    @Test
    @DisplayName("export should generate valid Excel bytes with full book data")
    void export_ShouldGenerateValidExcelWithFullData() throws IOException {
        byte[] result = excelBookExportService.export(fullBook);

        assertThat(result).isNotEmpty();

        // Проверяем внутренности Excel файла, чтобы убедиться в корректности маппинга
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("Карточка книги");
            assertThat(sheet).isNotNull();

            // Проверяем шапку
            Row headerRow = sheet.getRow(0);
            assertThat(headerRow.getCell(0).getStringCellValue()).isEqualTo("Характеристика");
            assertThat(headerRow.getCell(1).getStringCellValue()).isEqualTo("Значение");

            // Проверяем ISBN
            assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo(fullBook.getIsbn());

            // Проверяем категории
            assertThat(sheet.getRow(2).getCell(1).getStringCellValue()).contains("Фантастика");

            // Проверяем название
            assertThat(sheet.getRow(3).getCell(1).getStringCellValue()).isEqualTo("Основание");

            // Проверяем авторов (так как Set не гарантирует порядок, проверяем через contains)
            String authorsValue = sheet.getRow(4).getCell(1).getStringCellValue();
            assertThat(authorsValue).contains("Айзек Азимов");
            assertThat(authorsValue).contains("Роберт Хайнлайн");

            // Проверяем издателя
            assertThat(sheet.getRow(5).getCell(1).getStringCellValue()).isEqualTo("Эксмо");

            // Проверяем описание
            assertThat(sheet.getRow(6).getCell(1).getStringCellValue()).isEqualTo(fullBook.getDescription());
        }
    }

    @Test
    @DisplayName("export should handle null and empty fields correctly")
    void export_ShouldHandleNullAndEmptyFields() throws IOException {
        byte[] result = excelBookExportService.export(emptyBook);

        assertThat(result).isNotEmpty();

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("Карточка книги");
            assertThat(sheet).isNotNull();

            // Проверяем заглушки для пустых полей
            assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEmpty(); // null ISBN -> ""
            assertThat(sheet.getRow(2).getCell(1).getStringCellValue()).isEqualTo("Без категории");
            assertThat(sheet.getRow(4).getCell(1).getStringCellValue()).isEqualTo("Автор не указан");
            assertThat(sheet.getRow(5).getCell(1).getStringCellValue()).isEqualTo("Издатель не указан");
            assertThat(sheet.getRow(6).getCell(1).getStringCellValue()).isEmpty(); // null desc -> ""
        }
    }

    @Test
    @DisplayName("getSupportedFormat should return XLSX")
    void getSupportedFormat_ShouldReturnXlsx() {
        assertThat(excelBookExportService.getSupportedFormat()).isEqualTo(ExportFormat.XLSX);
    }
}