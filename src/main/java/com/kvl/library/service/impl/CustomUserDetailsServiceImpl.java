package com.kvl.library.service.impl;

import com.kvl.library.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Реализация стандартного интерфейса Spring Security для аутентификации пользователей.
 * <p>
 * Класс адаптирует доменную модель учетных записей ({@link UserRepository})
 * к механизмам проверки подлинности Spring Security. Используется JwtRequestFilter
 * и AuthenticationManager для верификации JWT-токенов при входе в систему.
 */
@Service
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Конструктор для внедрения зависимостей.
     *
     * @param userRepository репозиторий для управления сущностями пользователей
     */
    public CustomUserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Загружает данные пользователя по его уникальному логину (username).
     * <p>
     * Метод помечен как {@code @Transactional(readOnly = true)}, так как он выполняет
     * исключительно операцию чтения из базы данных при каждой валидации токена.
     *
     * @param username логин пользователя, запрашивающего доступ
     * @return заполненный объект {@link UserDetails} для контекста безопасности
     * @throws UsernameNotFoundException если пользователь с указанным логином отсутствует в системе
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));
    }
}