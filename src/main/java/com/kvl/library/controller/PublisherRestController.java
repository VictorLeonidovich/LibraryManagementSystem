package com.kvl.library.controller;

import com.kvl.library.dto.PublisherRequestDTO;
import com.kvl.library.dto.PublisherResponseDTO;
import com.kvl.library.entity.Publisher;
import com.kvl.library.mapper.PublisherMapper;
import com.kvl.library.service.PublisherService;
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
@RequestMapping("/api/v1/publishers")
@RequiredArgsConstructor
public class PublisherRestController {

    private final PublisherService publisherService;
    private final PublisherMapper publisherMapper;

    // GET /api/v1/publishers?name=Молодая&page=0&size=10&sort=name,asc
    @GetMapping
    public ResponseEntity<Page<PublisherResponseDTO>> getAllPublishers(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {

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
    public ResponseEntity<PublisherResponseDTO> getPublisherById(@PathVariable Long id) {
        Publisher publisher = publisherService.findPublisherById(id);
        return ResponseEntity.ok(publisherMapper.toResponseDTO(publisher));
    }

    // POST /api/v1/publishers
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PublisherResponseDTO> createPublisher(@Valid @RequestBody PublisherRequestDTO requestDTO) {
        Publisher publisher = publisherMapper.toEntity(requestDTO);
        publisherService.createPublisher(publisher);
        return ResponseEntity.status(HttpStatus.CREATED).body(publisherMapper.toResponseDTO(publisher));
    }

    // PUT /api/v1/publishers/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PublisherResponseDTO> updatePublisher(@PathVariable Long id,
                                                                @Valid @RequestBody PublisherRequestDTO requestDTO) {
        Publisher existingPublisher = publisherService.findPublisherById(id);
        publisherMapper.updateEntityFromDto(requestDTO, existingPublisher);
        publisherService.updatePublisher(existingPublisher);
        return ResponseEntity.ok(publisherMapper.toResponseDTO(existingPublisher));
    }

    // DELETE /api/v1/publishers/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePublisher(@PathVariable Long id) {
        publisherService.deletePublisher(id);
        return ResponseEntity.noContent().build();
    }
}