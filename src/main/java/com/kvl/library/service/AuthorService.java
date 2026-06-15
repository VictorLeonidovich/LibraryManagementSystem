package com.kvl.library.service;

import com.kvl.library.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для управления авторами книг.
 */
public interface AuthorService {

    /**
     * Получить полный список всех авторов без пагинации.
     * Используется в основном для выпадающих списков (dropdown) в UI.
     *
     * @return список всех авторов
     */
    @Transactional(readOnly = true)
    List<Author> findAllAuthors();

    /**
     * Получить страницу авторов.
     * Используется для постраничного вывода общего списка авторов в REST API или UI.
     *
     * @param pageable параметры пагинации и сортировки
     * @return страница с авторами
     */
    @Transactional(readOnly = true)
    Page<Author> findAllAuthors(Pageable pageable);

    /**
     * Поиск авторов по имени с поддержкой пагинации.
     * Поиск нечувствителен к регистру символов.
     *
     * @param name     строка для поиска в имени автора
     * @param pageable параметры пагинации и сортировки
     * @return страница с найденными авторами
     */
    @Transactional(readOnly = true)
    Page<Author> searchAuthorsByName(String name, Pageable pageable);

    /**
     * Найти автора по его уникальному идентификатору.
     *
     * @param id уникальный идентификатор автора
     * @return найденный автор
     * @throws com.kvl.library.exception.EntityNotFoundException если автор с таким ID не найден
     */
    @Transactional(readOnly = true)
    Author findAuthorById(Long id);

    /**
     * Создать и сохранить нового автора.
     *
     * @param author сущность нового автора для сохранения
     */
    @Transactional
    void createAuthor(Author author);

    /**
     * Обновить данные существующего автора.
     * Перед обновлением выполняется проверка существования сущности в БД.
     *
     * @param author сущность автора с обновленными данными
     * @throws com.kvl.library.exception.EntityNotFoundException если автора с таким ID нет в базе данных
     */
    @Transactional
    void updateAuthor(Author author);

    /**
     * Удалить автора по его уникальному идентификатору.
     *
     * @param id уникальный идентификатор автора для удаления
     * @throws com.kvl.library.exception.EntityNotFoundException если автор с таким ID не найден
     */
    @Transactional
    void deleteAuthor(Long id);
}