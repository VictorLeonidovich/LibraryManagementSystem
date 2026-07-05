package com.kvl.library.service.core.impl;

import com.kvl.library.entity.Publisher;
import com.kvl.library.exception.EntityNotFoundException;
import com.kvl.library.repository.PublisherRepository;
import com.kvl.library.service.core.PublisherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Реализация сервиса управления контрагентами-издательствами.
 * <p>
 * Класс обеспечивает изоляцию транзакционного контекста и валидацию жизненного цикла
 * сущностей перед отправкой команд в слой доступа к данным ({@link PublisherRepository}).
 */
@Slf4j
@Service
public class PublisherServiceImpl implements PublisherService {

    private final PublisherRepository publisherRepository;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param publisherRepository репозиторий для управления сущностями издательств
     */
    public PublisherServiceImpl(PublisherRepository publisherRepository) {
        this.publisherRepository = publisherRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Publisher> findAllPublishers() {
        log.info("Fetching all publishers as a list for MVC interface");
        return publisherRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Publisher> findAllPublishers(Pageable pageable) {
        log.info("Fetching a page of publishers from the database");
        return publisherRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Publisher> searchPublishersByName(String name, Pageable pageable) {
        log.info("Searching publishers by name containing '{}'", name);
        return publisherRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Publisher findPublisherById(final Long id) {
        Publisher publisher = findById(id);
        log.info("Fetched publisher '{}' by id '{}' from the database", publisher, id);
        return publisher;
    }

    /**
     * Внутренний приватный вспомогательный метод для поиска издательства по ID.
     * Выбрасывает бизнес-исключение в случае отсутствия записи в базе данных.
     *
     * @param id уникальный идентификатор издательства
     * @return найденная сущность издательства
     * @throws EntityNotFoundException если издательство с таким ID не существует
     */
    private Publisher findById(final Long id) {
        return publisherRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Publisher with ID " + id + " was not found"));
    }

    @Override
    @Transactional
    public void createPublisher(final Publisher publisher) {
        log.info("Saving publisher '{}' to the database", publisher);
        publisherRepository.save(publisher);
    }

    @Override
    @Transactional
    public void updatePublisher(final Publisher publisher) {
        log.info("Updating publisher '{}' in the database", publisher);
        // Защита от нежелательного INSERT при некорректных запросах обновления
        if (!publisherRepository.existsById(publisher.getId())) {
            throw new EntityNotFoundException("Publisher with ID " + publisher.getId() + " was not found");
        }
        publisherRepository.save(publisher);
    }

    @Override
    @Transactional
    public void deletePublisher(final Long id) {
        final Publisher publisher = findById(id);
        log.info("Deleting publisher '{}' by id '{}' from the database", publisher, id);
        publisherRepository.deleteById(publisher.getId());
    }
}