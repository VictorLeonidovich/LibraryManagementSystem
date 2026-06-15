package com.kvl.library.service.impl;

import com.kvl.library.entity.Category;
import com.kvl.library.exception.EntityNotFoundException;
import com.kvl.library.repository.CategoryRepository;
import com.kvl.library.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Реализация сервиса категорий и жанров.
 * <p>
 * Отвечает за логику изоляции сред хранения категориальных метаданных.
 * Интегрирует проверки безопасности транзакций при каскадном или изолированном удалении.
 */
@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param categoryRepository репозиторий для управления категориями
     */
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAllCategories() {
        log.info("Fetching all categories as a list for MVC interface");
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Category> findAllCategories(Pageable pageable) {
        log.info("Fetching a page of categories from the database");
        return categoryRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Category> searchCategoriesByName(String name, Pageable pageable) {
        log.info("Searching categories by name containing '{}'", name);
        return categoryRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Category findCategoryById(final Long id) {
        Category category = findById(id);
        log.info("Fetched category '{}' by id '{}' from the database", category, id);
        return category;
    }

    /**
     * Приватный метод получения категории по первичному ключу.
     *
     * @param id первичный ключ категории
     * @return найденная категория
     * @throws EntityNotFoundException если запись не найдена
     */
    private Category findById(final Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category with ID " + id + " was not found"));
    }

    @Override
    @Transactional
    public void createCategory(final Category category) {
        log.info("Saving category '{}' to the database", category);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void updateCategory(final Category category) {
        log.info("Updating category '{}' in the database", category);
        if (!categoryRepository.existsById(category.getId())) {
            throw new EntityNotFoundException("Category with ID " + category.getId() + " was not found");
        }
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(final Long id) {
        final Category category = findById(id);
        log.info("Deleting category '{}' by id '{}' from the database", category, id);
        categoryRepository.deleteById(category.getId());
    }
}