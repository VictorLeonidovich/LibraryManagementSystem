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
 * Сущность, представляющая автора литературных произведений.
 * <p>
 * Связана отношением многие-ко-многим с сущностью {@link Book}. Изменения состава
 * авторов каскадно транслируются на связанные книги.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "authors")
@ToString(onlyExplicitlyIncluded = true)
public class Author {

    /**
     * Первичный ключ автора в таблице БД.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    /**
     * Уникальное имя или псевдоним автора. Снабжено ограничениями длины и валидацией на пустоту.
     */
    @NotEmpty(message = "Имя не должно быть пустым")
    @Size(min = 2, max = 100, message = "Имя должно быть длиной от 2 до 100 символов")
    @Column(name = "name", length = 100, nullable = false, unique = true)
    @ToString.Include
    private String name;

    /**
     * Краткое биографическое описание автора.
     */
    @NotEmpty(message = "Описание не должно быть пустым")
    @Size(min = 2, max = 250, message = "Описание должно быть длиной от 2 до 250 символов")
    @Column(name = "description", length = 250, nullable = false)
    @ToString.Include
    private String description;

    /**
     * Набор книг, написанных данным автором или в соавторстве.
     * Ведомая сторона двунаправленной связи многие-ко-многим (mappedBy).
     */
    @ManyToMany(mappedBy = "authors", cascade = CascadeType.ALL)
    private Set<Book> books = new HashSet<>();

    /**
     * Бизнес-конструктор для быстрой инициализации карточки автора.
     *
     * @param name        имя автора
     * @param description краткая биография
     */
    public Author(String name, String description) {
        this.name = name;
        this.description = description;
    }
}