package com.kvl.library.controller;

import com.kvl.library.entity.Book;
import com.kvl.library.service.AuthorService;
import com.kvl.library.service.BookService;
import com.kvl.library.service.CategoryService;
import com.kvl.library.service.PublisherService;
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

    @GetMapping("/books")
    public String findAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Book> booksPage = bookService.findAllBooks(pageable);

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
}