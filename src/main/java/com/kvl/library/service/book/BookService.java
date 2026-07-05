package com.kvl.library.service.book;

import com.kvl.library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для управления книгами в библиотечной системе.
 */
public interface BookService {

    /**
     * Получить полный список всех книг без пагинации.
     * Применяется для классического MVC-интерфейса (Thymeleaf UI).
     *
     * @return список всех книг
     */
    @Transactional(readOnly = true)
    List<Book> findAllBooks();

    /**
     * Получить страницу книг.
     * Применяется для постраничного отображения каталога книг в REST API.
     *
     * @param pageable параметры пагинации и сортировки
     * @return страница с книгами
     */
    @Transactional(readOnly = true)
    Page<Book> findAllBooks(Pageable pageable);

    /**
     * Поиск книг по ключевому слову с пагинацией.
     * Поиск осуществляется по совпадению в названии книги или её коде ISBN.
     *
     * @param keyword  ключевое слово для поиска (название или ISBN)
     * @param pageable параметры пагинации и сортировки
     * @return страница с найденными книгами
     */
    @Transactional(readOnly = true)
    Page<Book> searchBooks(String keyword, Pageable pageable);

    /**
     * Найти книгу по её уникальному идентификатору.
     *
     * @param id уникальный идентификатор книги
     * @return найденная книга
     * @throws com.kvl.library.exception.EntityNotFoundException если книга с таким ID не найдена
     */
    @Transactional(readOnly = true)
    Book findBookById(Long id);

    /**
     * Создать и сохранить новую книгу.
     *
     * @param book сущность новой книги для сохранения
     */
    @Transactional
    void createBook(Book book);

    /**
     * Обновить данные существующей книги.
     * Содержит предохранитель от нежелательного INSERT при отправке веб-форм.
     *
     * @param book сущность книги с обновленными данными
     * @throws com.kvl.library.exception.EntityNotFoundException если книга с таким ID нет в базе данных
     */
    @Transactional
    void updateBook(Book book);

    /**
     * Удалить книгу по её уникальному идентификатору.
     *
     * @param id уникальный идентификатор книги для удаления
     * @throws com.kvl.library.exception.EntityNotFoundException если книга с таким ID не найдена
     */
    @Transactional
    void deleteBook(Long id);

    /**
     * Получить список ISBN популярных книг для главной страницы каталога.
     * Применяется для оптимизированного вывода аналитических данных.
     *
     * @return список строк с ISBN популярных книг
     */
    @Transactional(readOnly = true)
    java.util.List<String> findPopularBookIsbns();
}