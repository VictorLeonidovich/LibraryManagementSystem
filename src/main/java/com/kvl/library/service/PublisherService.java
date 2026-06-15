package com.kvl.library.service;

import com.kvl.library.entity.Publisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для управления издательствами книг в библиотечной системе.
 * Отвечает за обеспечение бизнес-логики учета и актуализации данных компаний-издателей.
 */
public interface PublisherService {

    /**
     * Получить полный список всех издательств без пагинации.
     * Используется для построения выпадающих списков выбора в MVC-интерфейсе (Thymeleaf UI).
     *
     * @return список всех зарегистрированных издательств
     */
    @Transactional(readOnly = true)
    List<Publisher> findAllPublishers();

    /**
     * Получить страницу издательств с учетом параметров пагинации.
     * Применяется для постраничного отображения каталога контрагентов в REST API.
     *
     * @param pageable параметры пагинации, смещения и направления сортировки
     * @return страница с объектами издательств
     */
    @Transactional(readOnly = true)
    Page<Publisher> findAllPublishers(Pageable pageable);

    /**
     * Поиск издательств по названию с поддержкой пагинации.
     * Поиск выполняется по частичному совпадению и нечувствителен к регистру символов.
     *
     * @param name     строка или ключевое слово для поиска в названии издательства
     * @param pageable параметры пагинации и сортировки результатов
     * @return страница с найденными издательствами
     */
    @Transactional(readOnly = true)
    Page<Publisher> searchPublishersByName(String name, Pageable pageable);

    /**
     * Найти конкретное издательство по его уникальному идентификатору.
     *
     * @param id уникальный числовой идентификатор издательства в базе данных
     * @return найденная сущность издательства
     * @throws com.kvl.library.exception.EntityNotFoundException если запись с указанным ID отсутствует в системе
     */
    @Transactional(readOnly = true)
    Publisher findPublisherById(Long id);

    /**
     * Создать и сохранить новое издательство в системе.
     *
     * @param publisher объект нового издательства для персистенции
     */
    @Transactional
    void createPublisher(Publisher publisher);

    /**
     * Обновить данные существующего в базе данных издательства.
     * Содержит обязательную предварительную валидацию идентификатора (предохранитель),
     * исключающую случайное дублирование записей.
     *
     * @param publisher объект издательства с измененными полями и заполненным ID
     * @throws com.kvl.library.exception.EntityNotFoundException если обновляемое издательство не найдено по ID
     */
    @Transactional
    void updatePublisher(Publisher publisher);

    /**
     * Удалить издательство из системы по его уникальному идентификатору.
     * Перед удалением производится транзакционная проверка фактического существования записи в БД.
     *
     * @param id уникальный идентификатор издательства, подлежащего удалению
     * @throws com.kvl.library.exception.EntityNotFoundException если удаляемое издательство не найдено в системе
     */
    @Transactional
    void deletePublisher(Long id);
}