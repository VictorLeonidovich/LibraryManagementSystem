package com.kvl.library.controller;

import com.kvl.library.entity.Publisher;
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
public class PublisherController {

    private final PublisherService publisherService;

    @GetMapping("/publishers")
    public String findAllPublishers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Publisher> publishersPage = publisherService.findAllPublishers(pageable);

        // Передаем контент и метаданные пагинации в Thymeleaf
        model.addAttribute("publishers", publishersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", publishersPage.getTotalPages());
        model.addAttribute("totalItems", publishersPage.getTotalElements());
        model.addAttribute("size", size);

        return "publishers";
    }

    @GetMapping("/remove-publisher/{id}")
    public String removePublisher(@PathVariable Long id) {
        publisherService.deletePublisher(id);
        return "redirect:/publishers";
    }

    @GetMapping("/update-publisher/{id}")
    public String updatePublisher(@PathVariable Long id, Model model) {
        model.addAttribute("publisher", publisherService.findPublisherById(id));
        return "update-publisher";
    }

    @PostMapping("/save-publisher/{id}")
    public String savePublisher(@PathVariable Long id, @Valid Publisher publisher, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "update-publisher";
        }
        publisherService.updatePublisher(publisher);
        return "redirect:/publishers";
    }

    @GetMapping("/add-publisher")
    public String addPublisher(Publisher publisher) {
        return "add-publisher";
    }

    @PostMapping("/save-publisher")
    public String savePublisher(@Valid Publisher publisher, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "add-publisher";
        }
        publisherService.createPublisher(publisher);
        return "redirect:/publishers";
    }
}