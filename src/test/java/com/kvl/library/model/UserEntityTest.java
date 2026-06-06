package com.kvl.library.model;

import com.kvl.library.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User Entity Contract Tests")
class UserEntityTest {

    @Test
    @DisplayName("Should verify Spring Security UserDetails flags")
    void testUserDetailsMethods() {
        User user = new User();
        user.setRole("ROLE_USER");

        // Проверяем дефолтные флаги безопасности
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
        assertThat(user.isEnabled()).isTrue();

        // Проверяем маппинг ролей в Authorities
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("Should verify toString contains only id and username")
    void testToStringContract() {
        User user = new User();
        user.setId(42L);
        user.setUsername("test_library_user");
        user.setPassword("secret_hash"); // Пароль не должен быть в toString

        String toStringResult = user.toString();

        assertThat(toStringResult).contains("id=42", "username=test_library_user");
        assertThat(toStringResult).doesNotContain("secret_hash");
    }

    @Test
    @DisplayName("Should verify equals and hashCode contracts strictly by ID")
    void testEqualsAndHashCode() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(1L);
        user2.setUsername("user2"); // Имя другое, но ID одинаковый

        User user3 = new User();
        user3.setId(2L);

        User nullIdUser1 = new User();
        User nullIdUser2 = new User();

        // 1. Сравнение с самим собой и null
        assertThat(user1.equals(user1)).isTrue();
        assertThat(user1.equals(null)).isFalse();
        assertThat(user1.equals(new Object())).isFalse();

        // 2. Сравнение по ID (бизнес-логика JPA)
        assertThat(user1.equals(user2)).isTrue();
        assertThat(user1.equals(user3)).isFalse();

        // 3. Объекты без ID не должны быть равны друг другу
        assertThat(nullIdUser1.equals(nullIdUser2)).isFalse();

        // 4. Контракт hashCode
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
        assertThat(user1.hashCode()).isEqualTo(user3.hashCode()); // это правильное и ожидаемое поведение JPA-модели
        assertThat(nullIdUser1.hashCode()).isEqualTo(nullIdUser1.getClass().hashCode());
    }
}