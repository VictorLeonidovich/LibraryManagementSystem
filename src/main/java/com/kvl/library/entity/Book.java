package com.kvl.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

/**
 * Центральная доменная модель системы, представляющая книгу в библиотечном фонде.
 * <p>
 * Класс выступает владеющей стороной (Owning Side) для реляционных связей многие-ко-многим
 * с авторами, категориями и издательствами. Содержит вспомогательные методы (utility methods)
 * для обеспечения ссылочной целостности двунаправленных ассоциаций в оперативной памяти.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "books")
@ToString(onlyExplicitlyIncluded = true)
public class Book {

    /**
     * Уникальный инвентарный идентификатор книги в базе данных.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    /**
     * Официальное наименование книги. Присутствует валидация длины и обязательности заполнения.
     */
    @NotEmpty(message = "Имя не должно быть пустым")
    @Size(min = 2, max = 50, message = "Имя должно быть длиной от 2 до 50 символов")
    @Column(name = "name", length = 50, nullable = false)
    @ToString.Include
    private String name;

    /**
     * Международный стандартный книжный номер (ISBN). Уникальное индексируемое поле.
     */
    @NotEmpty(message = "ISBN не должно быть пустым")
    @Size(min = 2, max = 50, message = "ISBN должно быть длиной от 2 до 50 символов")
    @Column(name = "isbn", length = 50, nullable = false, unique = true)
    @ToString.Include
    private String isbn;

    /**
     * Аннотация или краткое текстовое описание содержания книги.
     */
    @NotEmpty(message = "Описание не должно быть пустым")
    @Size(min = 2, max = 250, message = "Описание должно быть длиной от 2 до 250 символов")
    @Column(name = "description", length = 250, nullable = false)
    @ToString.Include
    private String description;

    /**
     * Набор авторов, написавших данное произведение.
     * Связующая таблица: {@code books_authors}. Каскадность исключает автоматическое удаление автора при удалении книги.
     */
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinTable(name = "books_authors", joinColumns = {@JoinColumn(name = "book_id")}, inverseJoinColumns = {@JoinColumn(name = "author_id")})
    private Set<Author> authors = new HashSet<>();

    /**
     * Набор тематических категорий (жанров), к которым относится книга.
     * Связующая таблица: {@code books_categories}.
     */
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinTable(name = "books_categories", joinColumns = {@JoinColumn(name = "book_id")}, inverseJoinColumns = {@JoinColumn(name = "category_id")})
    private Set<Category> categories = new HashSet<>();

    /**
     * Набор издательств, выпустивших данное издание.
     * Связующая таблица: {@code books_publishers}.
     */
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinTable(name = "books_publishers", joinColumns = {@JoinColumn(name = "book_id")}, inverseJoinColumns = {@JoinColumn(name = "publisher_id")})
    private Set<Publisher> publishers = new HashSet<>();

    /**
     * Конструктор для инициализации базовых текстовых атрибутов книги.
     *
     * @param isbn        международный номер издания
     * @param name        название книги
     * @param description аннотация/описание
     */
    public Book(String isbn, String name, String description) {
        this.name = name;
        this.isbn = isbn;
        this.description = description;
    }

    /**
     * Удаляет автора из списка книги и синхронизирует обратную связь у сущности автора.
     *
     * @param author удаляемый автор
     */
    public void removeAuthor(final Author author) {
        authors.remove(author);
        author.getBooks().remove(author);
    }

    /**
     * Добавляет автора к книге и автоматически регистрирует текущую книгу в наборе у автора.
     *
     * @param author добавляемый автор
     */
    public void addAuthor(final Author author) {
        authors.add(author);
        author.getBooks().add(this);
    }

    /**
     * Удаляет категорию из списка книги и синхронизирует обратную связь у сущности категории.
     *
     * @param category удаляемая категория
     */
    public void removeCategory(final Category category) {
        categories.remove(category);
        category.getBooks().remove(category);
    }

    /**
     * Добавляет категорию к книге и автоматически регистрирует текущую книгу в наборе у категории.
     *
     * @param category добавляемая категория
     */
    public void addCategory(final Category category) {
        categories.add(category);
        category.getBooks().add(this);
    }

    /**
     * Удаляет издательство из списка книги и синхронизирует обратную связь у сущности издательства.
     *
     * @param publisher удаляемое издательство
     */
    public void removePublisher(final Publisher publisher) {
        publishers.remove(publisher);
        publisher.getBooks().remove(publisher);
    }

    /**
     * Добавляет издательство к книге и автоматически регистрирует текущую книгу в наборе у издательства.
     *
     * @param publisher добавляемое издательство
     */
    public void addPublisher(final Publisher publisher) {
        publishers.add(publisher);
        publisher.getBooks().add(this);
    }
}