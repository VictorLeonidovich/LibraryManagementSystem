package com.kvl.library.service.impl;

import com.kvl.library.entity.Author;
import com.kvl.library.entity.Book;
import com.kvl.library.entity.Category;
import com.kvl.library.entity.Publisher;
import com.kvl.library.enums.ExportFormat;
import com.lowagie.text.pdf.PdfReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("PdfBookExportServiceImpl Unit Tests")
class PdfBookExportServiceImplTest {

    @InjectMocks
    private PdfBookExportServiceImpl pdfBookExportService;

    private Book fullBook;
    private Book emptyBook;

    @BeforeEach
    void setUp() {
        // 1. Книга со всеми заполненными полями
        Category category = new Category();
        category.setName("Классика");

        Author author = new Author();
        author.setName("Лев Толстой");

        Publisher publisher = new Publisher();
        publisher.setName("Просвещение");

        fullBook = new Book();
        fullBook.setId(1L);
        fullBook.setIsbn("978-5-17-080000-0");
        fullBook.setName("Война и мир");
        fullBook.setDescription("Эпический роман-эпопея.");
        fullBook.setCategories(Set.of(category));
        fullBook.setAuthors(Set.of(author));
        fullBook.setPublishers(Set.of(publisher));

        // 2. Книга с пустыми коллекциями и null-значениями
        emptyBook = new Book();
        emptyBook.setId(2L);
        emptyBook.setIsbn(null);
        emptyBook.setName("Черновик");
        emptyBook.setDescription(null);
        emptyBook.setCategories(Collections.emptySet());
        emptyBook.setAuthors(null);
        emptyBook.setPublishers(Collections.emptySet());
    }

    @Test
    @DisplayName("export should generate valid PDF bytes with full book data")
    void export_ShouldGenerateValidPdfWithFullData() throws IOException {
        byte[] result = pdfBookExportService.export(fullBook);

        assertThat(result).isNotEmpty();

        // Проверяем валидность PDF структуры с помощью PdfReader из библиотеки OpenPDF
        try (PdfReader reader = new PdfReader(result)) {
            assertThat(reader.getNumberOfPages()).isGreaterThan(0);
            assertThat(reader.isEncrypted()).isFalse();
        }
    }

    @Test
    @DisplayName("export should handle null and empty fields correctly and build valid PDF")
    void export_ShouldHandleNullAndEmptyFields() throws IOException {
        byte[] result = pdfBookExportService.export(emptyBook);

        assertThat(result).isNotEmpty();

        // Проверяем, что даже с пустыми ветками (if/else) PDF успешно генерируется и читается
        try (PdfReader reader = new PdfReader(result)) {
            assertThat(reader.getNumberOfPages()).isGreaterThan(0);
        }
    }

    @Test
    @DisplayName("getSupportedFormat should return PDF")
    void getSupportedFormat_ShouldReturnPdf() {
        // Act & Assert
        assertThat(pdfBookExportService.getSupportedFormat()).isEqualTo(ExportFormat.PDF);
    }
}