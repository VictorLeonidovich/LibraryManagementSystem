package com.kvl.library.service.core;

import com.kvl.library.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для управления категориями (жанрами) книг в библиотечной системе.
 * Обеспечивает бизнес-логику распределения книг по тематическим разделам.
 */
public interface CategoryService {

    /**
     * Получить полный список всех категорий без пагинации.
     * Применяется для рендеринга классических MVC веб-интерфейсов (Thymeleaf UI),
     * а также для наполнения выпадающих списков выбора жанра при создании или редактировании книг.
     *
     * @return список всех доступных категорий
     */
    @Transactional(readOnly = true)
    List<Category> findAllCategories();

    /**
     * Получить страницу категорий с учетом параметров пагинации.
     * Применяется для постраничного отображения древа категорий или плоских таблиц в REST API.
     *
     * @param pageable параметры пагинации, смещения и сортировки данных
     * @return страница с объектами категорий
     */
    @Transactional(readOnly = true)
    Page<Category> findAllCategories(Pageable pageable);

    /**
     * Поиск категорий по текстовому совпадению в названии с поддержкой пагинации.
     * Поиск выполняется по частичному вхождению строки (LIKE) и нечувствителен к регистру (IgnoreCase).
     *
     * @param name     строка или ключевое слово для поиска в названии категории
     * @param pageable параметры пагинации и направления сортировки результатов
     * @return страница с найденными категориями, удовлетворяющими критерию поиска
     */
    @Transactional(readOnly = true)
    Page<Category> searchCategoriesByName(String name, Pageable pageable);

    /**
     * Найти конкретную категорию по её уникальному идентификатору.
     *
     * @param id уникальный числовой идентификатор категории в базе данных
     * @return найденная сущность категории
     * @throws com.kvl.library.exception.EntityNotFoundException если категория с указанным ID отсутствует в системе
     */
    @Transactional(readOnly = true)
    Category findCategoryById(Long id);

    /**
     * Создать и сохранить новую категорию (жанр) в системе.
     *
     * @param category объект новой категории для персистенции
     */
    @Transactional
    void createCategory(Category category);

    /**
     * Обновить метаданные существующей в базе данных категории.
     * Метод включает обязательную предварительную валидацию идентификатора (предохранитель),
     * исключающую случайное дублирование или создание новой записи (INSERT) вместо перезаписи.
     *
     * @param category объект категории с измененными полями и заполненным ID
     * @throws com.kvl.library.exception.EntityNotFoundException если обновляемая категория не найдена по ID
     */
    @Transactional
    void updateCategory(Category category);

    /**
     * Удалить категорию из системы по её уникальному идентификатору.
     * Перед удалением производится транзакционная проверка фактического существования записи в БД.
     *
     * @param id уникальный идентификатор категории, подлежащей удалению
     * @throws com.kvl.library.exception.EntityNotFoundException если удаляемая категория не найдена в системе
     */
    @Transactional
    void deleteCategory(Long id);
}