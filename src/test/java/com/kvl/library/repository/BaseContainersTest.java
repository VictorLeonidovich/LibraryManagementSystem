package com.kvl.library.repository;

import com.kvl.library.PostgresInitializer;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("containers")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class BaseContainersTest extends PostgresInitializer {
    // Чистый класс, наследующий СУБД для Data JPA тестов
}