package com.kvl.library.controller;

import com.kvl.library.dto.AuthorRequestDTO;
import com.kvl.library.dto.AuthorResponseDTO;
import com.kvl.library.entity.Author;
import com.kvl.library.mapper.AuthorMapper;
import com.kvl.library.service.AuthorService;
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
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
public class AuthorRestController {

    private final AuthorService authorService;
    private final AuthorMapper authorMapper;

    @GetMapping
    public ResponseEntity<Page<AuthorResponseDTO>> getAllAuthors(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {

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
    public ResponseEntity<AuthorResponseDTO> getAuthorById(@PathVariable Long id) {
        Author author = authorService.findAuthorById(id);
        return ResponseEntity.ok(authorMapper.toResponseDTO(author));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthorResponseDTO> createAuthor(@Valid @RequestBody AuthorRequestDTO requestDTO) {
        Author author = authorMapper.toEntity(requestDTO);
        authorService.createAuthor(author);
        return ResponseEntity.status(HttpStatus.CREATED).body(authorMapper.toResponseDTO(author));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthorResponseDTO> updateAuthor(@PathVariable Long id,
                                                          @Valid @RequestBody AuthorRequestDTO requestDTO) {
        Author existingAuthor = authorService.findAuthorById(id);
        authorMapper.updateEntityFromDto(requestDTO, existingAuthor);
        authorService.updateAuthor(existingAuthor);
        return ResponseEntity.ok(authorMapper.toResponseDTO(existingAuthor));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }
}