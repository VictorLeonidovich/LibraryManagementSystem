package com.kvl.library.service;

import com.kvl.library.entity.Category;
import com.kvl.library.exception.EntityNotFoundException;
import com.kvl.library.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Для Thymeleaf UI (обычный список)
    @Transactional(readOnly = true)
    public List<Category> findAllCategories() {
        log.info("Fetching all categories as a list for MVC interface");
        return categoryRepository.findAll();
    }

    // 1. Пагинация для общего списка REST (readOnly транзакция)
    @Transactional(readOnly = true)
    public Page<Category> findAllCategories(Pageable pageable) {
        log.info("Fetching a page of categories from the database");
        return categoryRepository.findAll(pageable);
    }

    // 2. Использование кастомного запроса с пагинацией для REST
    @Transactional(readOnly = true)
    public Page<Category> searchCategoriesByName(String name, Pageable pageable) {
        log.info("Searching categories by name containing '{}'", name);
        return categoryRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional(readOnly = true)
    public Category findCategoryById(final Long id) {
        Category category = findById(id);
        log.info("Fetched category '{}' by id '{}' from the database", category, id);
        return category;
    }

    private Category findById(final Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category with ID " + id + " was not found"));
    }

    @Transactional
    public void createCategory(final Category category) {
        log.info("Saving category '{}' to the database", category);
        categoryRepository.save(category);
    }

    @Transactional
    public void updateCategory(final Category category) {
        log.info("Updating category '{}' in the database", category);
        // Предохранитель от нежелательного INSERT при обновлении
        if (!categoryRepository.existsById(category.getId())) {
            throw new EntityNotFoundException("Category with ID " + category.getId() + " was not found");
        }
        categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(final Long id) {
        final Category category = findById(id);
        log.info("Deleting category '{}' by id '{}' from the database", category, id);
        categoryRepository.deleteById(category.getId());
    }
}