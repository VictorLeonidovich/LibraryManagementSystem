package com.kvl.library.controller.api;

import com.kvl.library.dto.CategoryRequestDTO;
import com.kvl.library.dto.CategoryResponseDTO;
import com.kvl.library.entity.Category;
import com.kvl.library.mapper.CategoryMapper;
import com.kvl.library.service.core.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "Категории", description = "Управление категориями книг (Доступ: USER/ADMIN)")
public class CategoryRestController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    // GET /api/v1/categories?name=Фант&page=0&size=10&sort=name,asc
    @GetMapping
    @Operation(summary = "Получить список категорий с пагинацией и фильтрацией",
            description = "Позволяет искать категории по совпадению в названии. Доступно всем аутентифицированным пользователям.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список категорий успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public ResponseEntity<Page<CategoryResponseDTO>> getAllCategories(
            @Parameter(description = "Название категории для поиска", example = "Фант")
            @RequestParam(required = false) String name,
            @ParameterObject @PageableDefault(size = 10, sort = "name") Pageable pageable) {

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
    @Operation(summary = "Получить категорию по её ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Категория найдена"),
            @ApiResponse(responseCode = "404", description = "Категория с таким ID отсутствует в базе данных")
    })
    public ResponseEntity<CategoryResponseDTO> getCategoryById(
            @Parameter(description = "Идентификатор категории", example = "5")
            @PathVariable Long id) {
        Category category = categoryService.findCategoryById(id);
        return ResponseEntity.ok(categoryMapper.toResponseDTO(category));
    }

    // POST /api/v1/categories
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать новую категорию", description = "Доступно только пользователям с ролью ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Категория успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные (ошибка валидации)"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (отсутствует роль ADMIN)")
    })
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryRequestDTO requestDTO) {
        Category category = categoryMapper.toEntity(requestDTO);
        categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryMapper.toResponseDTO(category));
    }

    // PUT /api/v1/categories/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить существующую категорию по ID", description = "Полное обновление полей категории. Доступно только ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные категории успешно обновлены"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена")
    })
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @Parameter(description = "Идентификатор обновляемой категории", example = "5")
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDTO requestDTO) {
        Category existingCategory = categoryService.findCategoryById(id);
        categoryMapper.updateEntityFromDto(requestDTO, existingCategory);
        categoryService.updateCategory(existingCategory);
        return ResponseEntity.ok(categoryMapper.toResponseDTO(existingCategory));
    }

    // DELETE /api/v1/categories/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить категорию по ID", description = "Доступно только пользователям с ролью ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Категория успешно удалена (нет содержимого)"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Категория не найдена")
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Идентификатор удаляемой категории", example = "5")
            @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}