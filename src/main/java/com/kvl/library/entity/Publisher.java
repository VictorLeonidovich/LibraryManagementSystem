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
 * Сущность, представляющая издательство (контрагента, выпустившего книгу).
 * <p>
 * Организует связь многие-ко-многим с каталогом книг ({@link Book}), поддерживая
 * сценарии, когда одно издание выпускается пулом или консорциумом издательских домов.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "publishers")
@ToString(onlyExplicitlyIncluded = true)
public class Publisher {

    /**
     * Первичный ключ записи издательства в БД.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    /**
     * Уникальное коммерческое наименование издательского дома.
     */
    @NotEmpty(message = "Имя не должно быть пустым")
    @Size(min = 2, max = 50, message = "Имя должно быть длиной от 2 до 50 символов")
    @Column(name = "name", length = 50, nullable = false, unique = true)
    @ToString.Include
    private String name;

    /**
     * Набор книг, опубликованных данным издательством.
     */
    @ManyToMany(mappedBy = "publishers", cascade = CascadeType.ALL)
    private Set<Book> books = new HashSet<>();

    /**
     * Бизнес-конструктор для быстрой инициализации издательства.
     *
     * @param name наименование организации
     */
    public Publisher(String name) {
        this.name = name;
    }
}