package com.kvl.library.controller;

import com.kvl.library.PostgresInitializer;
import com.kvl.library.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("containers")
@AutoConfigureMockMvc
@Transactional // Обеспечит одну общую сессию Hibernate внутри методов тестов
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseWebContainersTest extends PostgresInitializer {

    @Autowired
    protected MockMvc mockMvc;

    // Внедряем репозитории для принудительной очистки между классами тестов
    @Autowired private BookRepository bookRepository;
    @Autowired private AuthorRepository authorRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private PublisherRepository publisherRepository;
    @Autowired private UserRepository userRepository;

    @AfterEach
    void cleanDatabaseAfterTestClass() {
        // Гарантированно очищаем СУБД в правильном порядке связей
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        categoryRepository.deleteAll();
        publisherRepository.deleteAll();
        userRepository.deleteAll();
    }
}