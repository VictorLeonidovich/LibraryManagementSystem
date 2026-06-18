package com.kvl.library.security;

import com.kvl.library.entity.User;
import com.kvl.library.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Инициализатор пользователей в базе данных.
 * <p>
 * Срабатывает при старте или обновлении контекста приложения. Гарантирует наличие
 * базовых учетных записей (администратора и пользователя) для первоначального входа.
 */
@Slf4j
@Component
public class DatabaseUserInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.default-admin-password:password}")
    private String defaultAdminPassword;

    @Value("${app.security.default-user-password:password}")
    private String defaultUserPassword;

    public DatabaseUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        initializeDefaultUser("admin", defaultAdminPassword, "ROLE_ADMIN");
        initializeDefaultUser("user", defaultUserPassword, "ROLE_USER");
    }

    /**
     * Создает учетную запись по умолчанию, если она отсутствует в системе.
     *
     * @param username имя пользователя
     * @param defaultPassword незахешированный дефолтный пароль
     * @param role роль в формате "ROLE_XXX"
     */
    private void initializeDefaultUser(String username, String defaultPassword, String role) {
        if (!userRepository.findByUsername(username).isPresent()) {
            log.info("Default user '{}' not found. Initializing with role '{}'...", username, role);

            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(defaultPassword));
            user.setRole(role);

            userRepository.save(user);
            log.info("Default user '{}' successfully created.", username);
        } else {
            log.debug("User '{}' already exists. Skipping initialization to prevent password overwrites.", username);
        }
    }
}