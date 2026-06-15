package com.kvl.library.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Сущность пользователя системы, интегрированная со Spring Security.
 * <p>
 * Реализует интерфейс {@link UserDetails} для обеспечения аутентификации
 * и авторизации. Настройки логирования и переопределения методов адаптированы
 * под специфику работы Hibernate ORM (включая работу с прокси-объектами).
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@RequiredArgsConstructor
@ToString(onlyExplicitlyIncluded = true) // Защищает от циклического вызова связанных сущностей в toString
public class User implements UserDetails {

    /**
     * Уникальный числовой идентификатор пользователя в базе данных.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include // В логи будет выводиться только id и username
    private Long id;

    /**
     * Уникальное имя пользователя (логин) для входа в систему. Поле обязательно для заполнения.
     */
    @Column(unique = true, nullable = false)
    @ToString.Include
    private String username;

    /**
     * Хэшированная строка пароля пользователя.
     * Исключена из методов логирования и сериализации в целях безопасности.
     */
    @Column(nullable = false)
    // Пароль исключаем из toString из соображений безопасности
    private String password;

    /**
     * Строковое представление роли пользователя (например, "ROLE_USER", "ROLE_ADMIN").
     */
    private String role;

    /**
     * Возвращает коллекцию прав (авторитетов), предоставленных пользователю на основе его роли.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    /**
     * Проверяет, не истек ли срок действия учетной записи.
     * @return {@code true}, если аккаунт активен и не просрочен
     */
    @Override
    public boolean isAccountNonExpired() {
        return true; // Аккаунт не просрочен
    }

    /**
     * Проверяет, не заблокирован ли пользователь.
     * @return {@code true}, если аккаунт не заблокирован
     */
    @Override
    public boolean isAccountNonLocked() {
        return true; // Аккаунт не заблокирован
    }

    /**
     * Проверяет, не истек ли срок действия пароля (учетных данных).
     * @return {@code true}, если пароль действителен
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Пароль не просрочен
    }

    /**
     * Проверяет, включена ли учетная запись.
     * @return {@code true}, если пользователь активен
     */
    @Override
    public boolean isEnabled() {
        return true; // Аккаунт активен
    }

    /**
     * Выполняет безопасное сравнение объектов сущностей с учетом прокси-классов Hibernate Lazy Loading.
     * Сравнение производится исключительно по бизнес-ключу (идентификатору {@code id}).
     *
     * @param o объект для сравнения
     * @return {@code true}, если объекты представляют одну и ту же запись в БД
     */
    // Безопасный equals для JPA-сущностей, учитывающий прокси-классы Hibernate
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    /**
     * Возвращает стабильный хэш-код. Для обеспечения контракта {@code equals/hashCode}
     * во всех состояниях жизненного цикла JPA (transient, managed, detached) возвращает
     * хэш-код класса, а не динамических полей.
     */
    // Безопасный hashCode, возвращающий константное значение для объектов до их сохранения в БД
    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}