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
 * Сущность, представляющая тематическую категорию или жанр книг.
 * <p>
 * Позволяет классифицировать книжный фонд библиотеки. Связана отношением Many-to-Many
 * с книгами, что позволяет одной книге находиться в нескольких категориях одновременно.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "categories")
@ToString(onlyExplicitlyIncluded = true)
public class Category {

    /**
     * Первичный ключ категории в таблице БД.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    /**
     * Уникальное наименование жанра/категории.
     */
    @NotEmpty(message = "Имя не должно быть пустым")
    @Size(min = 2, max = 50, message = "Имя должно быть длиной от 2 до 50 символов")
    @Column(name = "name", length = 50, nullable = false, unique = true)
    @ToString.Include
    private String name;

    /**
     * Набор книг, входящих в данную жанровую категорию.
     */
    @ManyToMany(mappedBy = "categories", cascade = CascadeType.ALL)
    private Set<Book> books = new HashSet<>();

    /**
     * Бизнес-конструктор для быстрой инициализации категории.
     *
     * @param name наименование категории/жанра
     */
    public Category(String name) {
        this.name = name;
    }
}