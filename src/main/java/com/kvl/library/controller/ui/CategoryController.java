package com.kvl.library.controller.ui;

import com.kvl.library.entity.Category;
import com.kvl.library.service.CategoryService;
import jakarta.validation.Valid;
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
public class CategoryController {
    public static final String CATEGORIES = "categories";
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // Добавляем параметры пагинации по умолчанию (размер 5, сортировка по имени)
    @GetMapping("/categories")
    public String findAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Category> categoriesPage = categoryService.findAllCategories(pageable);

        // Передаем контент и метаданные пагинации в шаблон
        model.addAttribute(CATEGORIES, categoriesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categoriesPage.getTotalPages());
        model.addAttribute("totalItems", categoriesPage.getTotalElements());
        model.addAttribute("size", size);

        return CATEGORIES;
    }

    @GetMapping("/remove-category/{id}")
    public String removeCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        // Редирект предотвращает падение шаблона из-за отсутствия метаданных пагинации
        return "redirect:/categories";
    }

    @GetMapping("/update-category/{id}")
    public String updateCategory(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.findCategoryById(id));
        return "update-category";
    }

    @PostMapping("/save-category/{id}")
    public String saveCategory(@PathVariable Long id, @Valid Category category, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "update-category";
        }
        categoryService.updateCategory(category);
        return "redirect:/categories";
    }

    @GetMapping("/add-category")
    public String addCategory(Category category) {
        return "add-category";
    }

    @PostMapping("/save-category")
    public String saveCategory(@Valid Category category, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "add-category";
        }
        categoryService.createCategory(category);
        return "redirect:/categories";
    }
}