package com.kvl.library.security;

import com.kvl.library.entity.User;
import com.kvl.library.repository.UserRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseUserInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Проверяем админа. Если нет — создаем, если есть — гарантируем правильный хэш
        User admin = userRepository.findByUsername("admin").orElse(new User());

        admin.setUsername("admin");
        // Генерируем хэш "на лету" именно тем энкодером, который сейчас активен в SecurityConfig
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setRole("ROLE_ADMIN");

        userRepository.save(admin);

        // То же самое для обычного пользователя
        User user = userRepository.findByUsername("user").orElse(new User());
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole("ROLE_USER");

        userRepository.save(user);
    }
}