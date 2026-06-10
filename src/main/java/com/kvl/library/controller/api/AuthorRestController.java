package com.kvl.library.controller.api;

import com.kvl.library.dto.AuthorRequestDTO;
import com.kvl.library.dto.AuthorResponseDTO;
import com.kvl.library.entity.Author;
import com.kvl.library.mapper.AuthorMapper;
import com.kvl.library.service.AuthorService;
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
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
@Tag(name = "Авторы", description = "Управление списком авторов (Доступ: USER/ADMIN)")
public class AuthorRestController {

    private final AuthorService authorService;
    private final AuthorMapper authorMapper;

    @GetMapping
    @Operation(summary = "Получить список авторов с пагинацией и фильтрацией",
            description = "Позволяет искать авторов по совпадению в имени. Доступно всем аутентифицированным пользователям.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список авторов успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public ResponseEntity<Page<AuthorResponseDTO>> getAllAuthors(
            @Parameter(description = "Имя автора для поиска", example = "Толстой")
            @RequestParam(required = false) String name,
            @ParameterObject @PageableDefault(size = 10, sort = "name") Pageable pageable) {

        Page<Author> authorsPage;
        if (name != null && !name.trim().isEmpty()) {
            authorsPage = authorService.searchAuthorsByName(name, pageable);
        } else {
            authorsPage = authorService.findAllAuthors(pageable);
        }

        Page<AuthorResponseDTO> responsePage = authorsPage.map(authorMapper::toResponseDTO);
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить автора по его ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Автор найден"),
            @ApiResponse(responseCode = "404", description = "Автор с таким ID отсутствует в базе данных")
    })
    public ResponseEntity<AuthorResponseDTO> getAuthorById(
            @Parameter(description = "Идентификатор автора", example = "12")
            @PathVariable Long id) {
        Author author = authorService.findAuthorById(id);
        return ResponseEntity.ok(authorMapper.toResponseDTO(author));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать нового автора", description = "Доступно только пользователям с ролью ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Автор успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные (ошибка валидации)"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (отсутствует роль ADMIN)")
    })
    public ResponseEntity<AuthorResponseDTO> createAuthor(@Valid @RequestBody AuthorRequestDTO requestDTO) {
        Author author = authorMapper.toEntity(requestDTO);
        authorService.createAuthor(author);
        return ResponseEntity.status(HttpStatus.CREATED).body(authorMapper.toResponseDTO(author));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить существующего автора по ID", description = "Полное обновление полей автора. Доступно только ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные автора успешно обновлены"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Автор не найден")
    })
    public ResponseEntity<AuthorResponseDTO> updateAuthor(
            @Parameter(description = "Идентификатор обновляемого автора", example = "12")
            @PathVariable Long id,
            @Valid @RequestBody AuthorRequestDTO requestDTO) {
        Author existingAuthor = authorService.findAuthorById(id);
        authorMapper.updateEntityFromDto(requestDTO, existingAuthor);
        authorService.updateAuthor(existingAuthor);
        return ResponseEntity.ok(authorMapper.toResponseDTO(existingAuthor));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить автора по ID", description = "Доступно только пользователям с ролью ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Автор успешно удален (нет содержимого)"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Автор не найден")
    })
    public ResponseEntity<Void> deleteAuthor(
            @Parameter(description = "Идентификатор удаляемой книги", example = "12")
            @PathVariable Long id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }
}