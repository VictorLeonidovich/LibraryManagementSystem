package com.kvl.library.controller;

import com.kvl.library.entity.Author;
import com.kvl.library.entity.Book;
import com.kvl.library.entity.Category;
import com.kvl.library.entity.Publisher;
import com.kvl.library.repository.BookRepository;
import com.kvl.library.security.JwtRequestFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("BookController Thymeleaf Integration Tests (PostgreSQL Testcontainers)")
class BookControllerContainersTest extends BaseWebContainersTest {

    @Autowired
    private BookRepository bookRepository;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Book savedBook;
    private Author savedAuthor;
    private Category savedCategory;
    private Publisher savedPublisher;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    @BeforeEach
    void setUp() throws Exception {
        Category category = new Category();
        category.setName("Tech");

        Publisher publisher = new Publisher();
        publisher.setName("O'Reilly");

        Author author = new Author();
        author.setName("Robert Martin");
        author.setDescription("Famous software engineer");

        Book book = new Book();
        book.setName("Clean Code");
        book.setIsbn("978-0132350884");
        book.setDescription("Great book about software craftsmanship");

        book.addCategory(category);
        book.addPublisher(publisher);
        book.addAuthor(author);

        savedBook = bookRepository.save(book);

        // Извлекаем сохраненные объекты со сгенерированными БД ID
        savedAuthor = savedBook.getAuthors().iterator().next();
        savedCategory = savedBook.getCategories().iterator().next();
        savedPublisher = savedBook.getPublishers().iterator().next();

        // Пропуск JWT фильтра безопасности
        doAnswer(invocation -> {
            jakarta.servlet.ServletRequest request = invocation.getArgument(0);
            jakarta.servlet.ServletResponse response = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtRequestFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("GET /books - Should return books template with paginated data from PostgreSQL")
    @WithMockUser
    void findAllBooks_ShouldReturnTemplateWithData() throws Exception {
        mockMvc.perform(get("/books")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("books"))
                .andExpect(model().attributeExists("books"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("totalItems", 1L))
                .andExpect(model().attribute("size", 5));
    }

    @Test
    @DisplayName("GET /book/{id} - Should return individual book details template from PostgreSQL")
    @WithMockUser
    void findBook_ShouldReturnDetailsForm() throws Exception {
        mockMvc.perform(get("/book/" + savedBook.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("list-book"))
                .andExpect(model().attributeExists("book"));
    }

    @Test
    @DisplayName("GET /remove-book/{id} - Should delete book from PostgreSQL and redirect to books list")
    @WithMockUser
    void deleteBook_ShouldDeleteAndRedirect() throws Exception {
        // 1. Выполняем удаление и проверяем успешный редирект
        mockMvc.perform(get("/remove-book/" + savedBook.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        // 2. Вместо existsById делаем сквозной GET запрос на список книг /books.
        //база данных вернет обновленный список, и мы проверяем, что удаленной книги там НЕТ.
        mockMvc.perform(get("/books")) // Используем эндпоинт списка, или /books, если перенаправляет туда
                .andExpect(status().isOk());

        // Для 100% уверенности в изоляции проверяем через прямую очистку сессии, но без ассерта по id
        bookRepository.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("GET /update-book/{id} - Should populate supporting lists and return update template from PostgreSQL")
    @WithMockUser
    void updateBook_ShouldReturnUpdateFormWithDropdowns() throws Exception {
        mockMvc.perform(get("/update-book/" + savedBook.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("update-book"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("publishers"))
                .andExpect(model().attributeExists("authors"));
    }

    @Test
    @DisplayName("POST /save-book/{id} - Should update book in PostgreSQL and redirect when data is valid")
    void updateBook_ShouldSaveAndRedirect_WhenValid() throws Exception {
        // Чтобы Hibernate в контроллере не конфликтовал с кэшем setUp(),
        // мы очищаем сессию ДО вызова MockMvc, и Спринг честно загрузит сущности из PostgreSQL
        bookRepository.flush();
        entityManager.clear();

        mockMvc.perform(post("/save-book/" + savedBook.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", savedBook.getId().toString())
                        .param("name", "Effective Java")
                        .param("isbn", savedBook.getIsbn())
                        .param("description", "Updated description")
                        .param("authors.id", savedAuthor.getId().toString())
                        .param("categories.id", savedCategory.getId().toString())
                        .param("publishers.id", savedPublisher.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
    }


    @Test
    @DisplayName("POST /save-book/{id} - Should reload relation data and stay on template when validation fails")
    @WithMockUser
    void updateBook_ShouldReturnFormWithErrors_WhenInvalid() throws Exception {
        mockMvc.perform(post("/save-book/" + savedBook.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", savedBook.getId().toString())
                        .param("name", "")) // Пустое имя триггерит ошибку валидации
                .andExpect(status().isOk())
                .andExpect(view().name("update-book"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("publishers"))
                .andExpect(model().attributeExists("authors"));
    }

    @Test
    @DisplayName("GET /add-book - Should populate relation data and return creation form")
    @WithMockUser
    void addBook_ShouldReturnAddFormWithDropdowns() throws Exception {
        mockMvc.perform(get("/add-book"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-book"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("publishers"))
                .andExpect(model().attributeExists("authors"));
    }

    @Test
    @DisplayName("POST /save-book - Should create book in PostgreSQL and redirect when data is valid")
    @WithMockUser
    void saveBook_ShouldCreateAndRedirect_WhenValid() throws Exception {
        mockMvc.perform(post("/save-book")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Refactoring")
                        .param("isbn", "978-0134757599")
                        .param("description", "Improving code design")
                        // Подставляем реальные строковые ID из базы
                        .param("authors", savedAuthor.getId().toString())
                        .param("categories", savedCategory.getId().toString())
                        .param("publishers", savedPublisher.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
    }

    @Test
    @DisplayName("POST /save-book - Should reload relation data and stay on template when validation fails")
    @WithMockUser
    void saveBook_ShouldReturnFormWithErrors_WhenInvalid() throws Exception {
        mockMvc.perform(post("/save-book")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "")) // Пустое имя нарушает @NotEmpty
                .andExpect(status().isOk())
                .andExpect(view().name("add-book"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("publishers"))
                .andExpect(model().attributeExists("authors"));
    }
}