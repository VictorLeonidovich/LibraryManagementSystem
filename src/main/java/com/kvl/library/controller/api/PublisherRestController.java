package com.kvl.library.controller.api;

import com.kvl.library.dto.PublisherRequestDTO;
import com.kvl.library.dto.PublisherResponseDTO;
import com.kvl.library.entity.Publisher;
import com.kvl.library.mapper.PublisherMapper;
import com.kvl.library.service.core.PublisherService;
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
@RequestMapping("/api/v1/publishers")
@RequiredArgsConstructor
@Tag(name = "Издатели", description = "Управление каталогом издательств (Доступ: USER/ADMIN)")
public class PublisherRestController {

    private final PublisherService publisherService;
    private final PublisherMapper publisherMapper;

    // GET /api/v1/publishers?name=Молодая&page=0&size=10&sort=name,asc
    @GetMapping
    @Operation(summary = "Получить список издателей с пагинацией и фильтрацией",
            description = "Позволяет искать издательства по совпадению в названии. Доступно всем аутентифицированным пользователям.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список издателей успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    public ResponseEntity<Page<PublisherResponseDTO>> getAllPublishers(
            @Parameter(description = "Название издательства для поиска", example = "Молодая")
            @RequestParam(required = false) String name,
            @ParameterObject @PageableDefault(size = 10, sort = "name") Pageable pageable) {

        Page<Publisher> publishersPage;
        if (name != null && !name.trim().isEmpty()) {
            publishersPage = publisherService.searchPublishersByName(name, pageable);
        } else {
            publishersPage = publisherService.findAllPublishers(pageable);
        }

        Page<PublisherResponseDTO> responsePage = publishersPage.map(publisherMapper::toResponseDTO);
        return ResponseEntity.ok(responsePage);
    }

    // GET /api/v1/publishers/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Получить издателя по его ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Издатель найден"),
            @ApiResponse(responseCode = "404", description = "Издатель с таким ID отсутствует в базе данных")
    })
    public ResponseEntity<PublisherResponseDTO> getPublisherById(
            @Parameter(description = "Идентификатор издателя", example = "3")
            @PathVariable Long id) {
        Publisher publisher = publisherService.findPublisherById(id);
        return ResponseEntity.ok(publisherMapper.toResponseDTO(publisher));
    }

    // POST /api/v1/publishers
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать новое издательство", description = "Доступно только пользователям с ролью ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Издатель успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные (ошибка валидации)"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (отсутствует роль ADMIN)")
    })
    public ResponseEntity<PublisherResponseDTO> createPublisher(@Valid @RequestBody PublisherRequestDTO requestDTO) {
        Publisher publisher = publisherMapper.toEntity(requestDTO);
        publisherService.createPublisher(publisher);
        return ResponseEntity.status(HttpStatus.CREATED).body(publisherMapper.toResponseDTO(publisher));
    }

    // PUT /api/v1/publishers/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить существующего издателя по ID", description = "Полное обновление полей издательства. Доступно только ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные издательства успешно обновлены"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Издатель не найден")
    })
    public ResponseEntity<PublisherResponseDTO> updatePublisher(
            @Parameter(description = "Идентификатор обновляемого издателя", example = "3")
            @PathVariable Long id,
            @Valid @RequestBody PublisherRequestDTO requestDTO) {
        Publisher existingPublisher = publisherService.findPublisherById(id);
        publisherMapper.updateEntityFromDto(requestDTO, existingPublisher);
        publisherService.updatePublisher(existingPublisher);
        return ResponseEntity.ok(publisherMapper.toResponseDTO(existingPublisher));
    }

    // DELETE /api/v1/publishers/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить издателя по ID", description = "Доступно только пользователям с ролью ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Издатель успешно удален (нет содержимого)"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Издатель не найден")
    })
    public ResponseEntity<Void> deletePublisher(
            @Parameter(description = "Идентификатор удаляемого издателя", example = "3")
            @PathVariable Long id) {
        publisherService.deletePublisher(id);
        return ResponseEntity.noContent().build();
    }
}