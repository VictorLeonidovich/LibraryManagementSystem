package com.kvl.library.repository;

import com.kvl.library.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserRepository Integration Tests with Testcontainers (PostgreSQL)")
class UserRepositoryTest extends BaseContainersTest {

    @Autowired
    private UserRepository userRepository;

    private User admin;
    private User manager;

    @BeforeEach
    void setUp() {
        // 1. Даем команду на очистку
        userRepository.deleteAll();

        // 2. Принудительно отправляем DELETE в PostgreSQL прямо сейчас
        userRepository.flush();

        // 3. Теперь база гарантированно чиста, и мы можем безопасно сохранять новых пользователей
        admin = new User();
        admin.setUsername("admin");
        admin.setPassword("password123");
        admin.setRole("ROLE_ADMIN");

        manager = new User();
        manager.setUsername("manager_ivan");
        manager.setPassword("securePass");
        manager.setRole("ROLE_MANAGER");

        // Сохраняем пользователей в тестовую БД
        userRepository.save(admin);
        userRepository.save(manager);

        // Дополнительно синхронизируем вставку
        userRepository.flush();
    }

    @Test
    @DisplayName("findByUsername should return user when exact username exists")
    void findByUsername_WhenUserExists_ShouldReturnUser() {
        Optional<User> foundUser = userRepository.findByUsername("admin");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("admin");
        assertThat(foundUser.get().getRole()).isEqualTo("ROLE_ADMIN");
        // Проверяем, что Spring Security методы интерфейса UserDetails тоже работают правильно
        assertThat(foundUser.get().isEnabled()).isTrue();
        assertThat(foundUser.get().getAuthorities()).hasSize(1);
    }

    @Test
    @DisplayName("findByUsername should return empty optional when username does not exist")
    void findByUsername_WhenUserDoesNotExist_ShouldReturnEmptyOptional() {
        Optional<User> foundUser = userRepository.findByUsername("unknown_user");

        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("save should persist new user with raw or encoded password data")
    void save_ShouldPersistUser() {
        User newUser = new User();
        newUser.setUsername("guest_user");
        newUser.setPassword("guestPass");
        newUser.setRole("ROLE_USER");

        User savedUser = userRepository.save(newUser);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(userRepository.findById(savedUser.getId())).isPresent();
    }

    @Test
    @DisplayName("deleteById should remove user from database permanently")
    void deleteById_WhenExists_ShouldRemoveUser() {
        userRepository.deleteById(admin.getId());
        Optional<User> deletedUser = userRepository.findById(admin.getId());

        assertThat(deletedUser).isEmpty();
    }
}