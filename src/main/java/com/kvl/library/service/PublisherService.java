package com.kvl.library.service;

import com.kvl.library.entity.Publisher;
import com.kvl.library.exception.EntityNotFoundException;
import com.kvl.library.repository.PublisherRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class PublisherService {
    private final PublisherRepository publisherRepository;

    public PublisherService(PublisherRepository publisherRepository) {
        this.publisherRepository = publisherRepository;
    }

    // Для классического интерфейса Thymeleaf UI
    @Transactional(readOnly = true)
    public List<Publisher> findAllPublishers() {
        log.info("Fetching all publishers as a list for MVC interface");
        return publisherRepository.findAll();
    }

    // 1. Пагинация общего списка для REST API
    @Transactional(readOnly = true)
    public Page<Publisher> findAllPublishers(Pageable pageable) {
        log.info("Fetching a page of publishers from the database");
        return publisherRepository.findAll(pageable);
    }

    // 2. Поиск по кастомному запросу с пагинацией для REST API
    @Transactional(readOnly = true)
    public Page<Publisher> searchPublishersByName(String name, Pageable pageable) {
        log.info("Searching publishers by name containing '{}'", name);
        return publisherRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional(readOnly = true)
    public Publisher findPublisherById(final Long id) {
        Publisher publisher = findById(id);
        log.info("Fetched publisher '{}' by id '{}' from the database", publisher, id);
        return publisher;
    }

    private Publisher findById(final Long id) {
        return publisherRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Publisher with ID " + id + " was not found"));
    }

    @Transactional
    public void createPublisher(final Publisher publisher) {
        log.info("Saving publisher '{}' to the database", publisher);
        publisherRepository.save(publisher);
    }

    @Transactional
    public void updatePublisher(final Publisher publisher) {
        log.info("Updating publisher '{}' in the database", publisher);
        // Защита от нежелательного INSERT
        if (!publisherRepository.existsById(publisher.getId())) {
            throw new EntityNotFoundException("Publisher with ID " + publisher.getId() + " was not found");
        }
        publisherRepository.save(publisher);
    }

    @Transactional
    public void deletePublisher(final Long id) {
        final Publisher publisher = findById(id);
        log.info("Deleting publisher '{}' by id '{}' from the database", publisher, id);
        publisherRepository.deleteById(publisher.getId());
    }
}