package com.kvl.library.controller.ui;

import com.kvl.library.entity.Book;
import com.kvl.library.enums.ExportAction;
import com.kvl.library.enums.ExportFormat;
import com.kvl.library.service.AuthorService;
import com.kvl.library.service.BookExportService;
import com.kvl.library.service.BookService;
import com.kvl.library.service.CategoryService;
import com.kvl.library.service.EmailService;
import com.kvl.library.service.PublisherService;
import com.kvl.library.service.impl.BookExportFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final CategoryService categoryService;
    private final PublisherService publisherService;
    private final AuthorService authorService;
    private final BookExportFactory bookExportFactory;
    private final EmailService emailService;

    @GetMapping("/books")
    public String findAllBooks(
            @RequestParam(required = false) String keyword, // Безопасный необязательный параметр для интеграции LIKE-поиска
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Book> booksPage;

        // Бизнес-логика фильтрации: перенаправляем запрос в кастомный LIKE-квери, если строка не пуста
        if (keyword != null && !keyword.trim().isEmpty()) {
            booksPage = bookService.searchBooks(keyword, pageable);
            model.addAttribute("keyword", keyword); // Передаем обратно, чтобы инпут в хедере не занулялся
        } else {
            booksPage = bookService.findAllBooks(pageable);
        }

        model.addAttribute("books", booksPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", booksPage.getTotalPages());
        model.addAttribute("totalItems", booksPage.getTotalElements());
        model.addAttribute("size", size);

        return "books";
    }

    @GetMapping("/book/{id}")
    public String findBook(@PathVariable Long id, Model model) {
        Book book = bookService.findBookById(id);
        model.addAttribute("book", book);
        return "list-book";
    }

    @GetMapping("/remove-book/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return "redirect:/books";
    }

    @GetMapping("/update-book/{id}")
    public String updateBook(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookService.findBookById(id));
        model.addAttribute("categories", categoryService.findAllCategories());
        model.addAttribute("publishers", publisherService.findAllPublishers());
        model.addAttribute("authors", authorService.findAllAuthors());
        return "update-book";
    }

    @PostMapping("/save-book/{id}")
    public String updateBook(@PathVariable Long id, @Valid Book book, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            // Если валидация провалилась, нужно заново прокинуть связанные списки для выпадающих меню формы
            model.addAttribute("categories", categoryService.findAllCategories());
            model.addAttribute("publishers", publisherService.findAllPublishers());
            model.addAttribute("authors", authorService.findAllAuthors());
            return "update-book";
        }
        bookService.updateBook(book);
        return "redirect:/books";
    }

    @GetMapping("/add-book")
    public String addBook(Book book, Model model) {
        model.addAttribute("categories", categoryService.findAllCategories());
        model.addAttribute("publishers", publisherService.findAllPublishers());
        model.addAttribute("authors", authorService.findAllAuthors());
        return "add-book";
    }

    @PostMapping("/save-book")
    public String saveBook(@Valid Book book, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAllCategories());
            model.addAttribute("publishers", publisherService.findAllPublishers());
            model.addAttribute("authors", authorService.findAllAuthors());
            return "add-book";
        }
        bookService.createBook(book);
        return "redirect:/books";
    }

    // Новый эндпоинт для обработки формы экспорта:
    @PostMapping("/books/{id}/export")
    public org.springframework.http.ResponseEntity<byte[]> exportBook(
            @PathVariable Long id,
            @RequestParam ExportFormat format,
            @RequestParam ExportAction action,
            @RequestParam(required = false) String email,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        // 1. Получаем книгу
        Book book = bookService.findBookById(id);

        // 2. Получаем нужную стратегию генерации (Excel или PDF) и генерируем байты
        BookExportService exportService = bookExportFactory.getService(format);
        byte[] fileContent = exportService.export(book);

        //String fileExtension = format.name().toLowerCase();
        String filename = "book_" + id + "_report." + (format == ExportFormat.XLSX ? "xlsx" : "pdf");

        // 3. Выбираем действие: скачать или отправить
        if (action == ExportAction.EMAIL) {
            String subject = "Карточка книги: " + book.getName();
            String text = "Здравствуйте!\n\nВо вложении находится выгрузка информации по книге \"" + book.getName() + "\".";

            emailService.sendEmailWithAttachment(email, subject, text, fileContent, filename);

            // Так как это POST-запрос с UI формы, для отправки почты сделаем редирект обратно с флеш-сообщением
            redirectAttributes.addFlashAttribute("successMessage", "Отчет успешно отправлен на " + email);

            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.FOUND)
                    .location(java.net.URI.create("/book/" + id))
                    .build();
        }

        // 4. Режим DOWNLOAD: отдаем файл в браузер
        String contentType = format == ExportFormat.XLSX
                ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                : "application/pdf";

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                .body(fileContent);
    }
}