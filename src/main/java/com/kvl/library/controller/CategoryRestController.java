package com.kvl.library.controller;

import com.kvl.library.dto.CategoryRequestDTO;
import com.kvl.library.dto.CategoryResponseDTO;
import com.kvl.library.entity.Category;
import com.kvl.library.mapper.CategoryMapper;
import com.kvl.library.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryRestController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    // GET /api/v1/categories?name=Фант&page=0&size=10&sort=name,asc
    @GetMapping
    public ResponseEntity<Page<CategoryResponseDTO>> getAllCategories(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {

        Page<Category> categoriesPage;
        if (name != null && !name.trim().isEmpty()) {
            categoriesPage = categoryService.searchCategoriesByName(name, pageable);
        } else {
            categoriesPage = categoryService.findAllCategories(pageable);
        }

        Page<CategoryResponseDTO> responsePage = categoriesPage.map(categoryMapper::toResponseDTO);
        return ResponseEntity.ok(responsePage);
    }

    // GET /api/v1/categories/{id}
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.findCategoryById(id);
        return ResponseEntity.ok(categoryMapper.toResponseDTO(category));
    }

    // POST /api/v1/categories
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryRequestDTO requestDTO) {
        Category category = categoryMapper.toEntity(requestDTO);
        categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryMapper.toResponseDTO(category));
    }

    // PUT /api/v1/categories/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable Long id,
                                                              @Valid @RequestBody CategoryRequestDTO requestDTO) {
        Category existingCategory = categoryService.findCategoryById(id);
        categoryMapper.updateEntityFromDto(requestDTO, existingCategory);
        categoryService.updateCategory(existingCategory);
        return ResponseEntity.ok(categoryMapper.toResponseDTO(existingCategory));
    }

    // DELETE /api/v1/categories/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}