package com.kvl.library.controller;

import com.kvl.library.entity.Author;
import com.kvl.library.service.AuthorService;
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
public class AuthorController {

    private final AuthorService authorService;

    // Добавляем поддержку параметров пагинации по умолчанию (страница 0, размер 5, сортировка по имени)
    @GetMapping("/authors")
    public String findAllAuthors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Author> authorsPage = authorService.findAllAuthors(pageable);

        // Передаем сам контент и метаданные пагинации в Thymeleaf
        model.addAttribute("authors", authorsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", authorsPage.getTotalPages());
        model.addAttribute("totalItems", authorsPage.getTotalElements());
        model.addAttribute("size", size);

        return "authors";
    }

    @GetMapping("/remove-author/{id}")
    public String removeAuthor(@PathVariable Long id) {
        authorService.deleteAuthor(id);
        // После удаления перенаправляем на первую страницу списка
        return "redirect:/authors";
    }

    @GetMapping("/update-author/{id}")
    public String updateAuthor(@PathVariable Long id, Model model) {
        model.addAttribute("author", authorService.findAuthorById(id));
        return "update-author";
    }

    @PostMapping("/save-author/{id}")
    public String saveAuthor(@PathVariable Long id, @Valid Author author, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "update-author";
        }
        authorService.updateAuthor(author);
        return "redirect:/authors";
    }

    @GetMapping("/add-author")
    public String addAuthor(Author author) {
        return "add-author";
    }

    @PostMapping("/save-author")
    public String saveAuthor(@Valid Author author, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "add-author";
        }
        authorService.createAuthor(author);
        return "redirect:/authors";
    }
}