package com.kvl.library.repository;

import com.kvl.library.entity.Author;
import com.kvl.library.entity.Book;
import com.kvl.library.entity.Category;
import com.kvl.library.entity.Publisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BookRepository Integration Tests with Testcontainers (PostgreSQL)")
class BookRepositoryTest extends BaseContainersTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    private Book warAndPeace;
    private Book EugeneOnegin;

    @BeforeEach
    void setUp() {
        // Очищаем базу данных в правильном порядке (сначала книги, так как они ссылаются на справочники)
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        categoryRepository.deleteAll();
        publisherRepository.deleteAll();

        // 1. Создаем и сохраняем зависимости
        Author tolstoy = authorRepository.save(new Author("Лев Толстой", "Классик мировой литературы."));
        Author pushkin = authorRepository.save(new Author("Александр Пушкин", "Великий русский поэт."));

        Category fiction = categoryRepository.save(new Category("Художественная литература"));
        Category poetry = categoryRepository.save(new Category("Поэзия"));

        Publisher eksmo = publisherRepository.save(new Publisher("Эксмо"));

        // 2. Создаем книги
        warAndPeace = new Book("978-5-699-12345-6", "Война и мир", "Исторический роман-эпопея.");
        warAndPeace.addAuthor(tolstoy);
        warAndPeace.addCategory(fiction);
        warAndPeace.addPublisher(eksmo);

        EugeneOnegin = new Book("978-5-699-54321-0", "Евгений Онегин", "Роман в стихах.");
        EugeneOnegin.addAuthor(pushkin);
        EugeneOnegin.addCategory(poetry);
        EugeneOnegin.addPublisher(eksmo);

        // 3. Сохраняем книги в базу данных
        bookRepository.save(warAndPeace);
        bookRepository.save(EugeneOnegin);
    }

    @Test
    @DisplayName("searchByNameOrIsbn should find book by part of its name regardless of case")
    void searchByNameOrIsbn_WhenNameMatches_ShouldReturnFilteredPage() {
        String keyword = "вОйНа"; // Проверяем регистронезависимость LOWER() в PostgreSQL
        Pageable pageable = PageRequest.of(0, 10);

        Page<Book> resultPage = bookRepository.searchByNameOrIsbn(keyword, pageable);

        assertThat(resultPage).isNotEmpty();
        assertThat(resultPage.getTotalElements()).isEqualTo(1);
        assertThat(resultPage.getContent().get(0).getName()).isEqualTo("Война и мир");
    }

    @Test
    @DisplayName("searchByNameOrIsbn should find book by part of its ISBN code")
    void searchByNameOrIsbn_WhenIsbnMatches_ShouldReturnFilteredPage() {
        String keyword = "54321"; // Часть ISBN книги Евгений Онегин
        Pageable pageable = PageRequest.of(0, 10);

        Page<Book> resultPage = bookRepository.searchByNameOrIsbn(keyword, pageable);

        assertThat(resultPage).isNotEmpty();
        assertThat(resultPage.getTotalElements()).isEqualTo(1);
        assertThat(resultPage.getContent().get(0).getName()).isEqualTo("Евгений Онегин");
    }

    @Test
    @DisplayName("searchByNameOrIsbn should return empty page when no matches found")
    void searchByNameOrIsbn_WhenNoMatch_ShouldReturnEmptyPage() {
        String keyword = "Чистый код";
        Pageable pageable = PageRequest.of(0, 10);

        Page<Book> resultPage = bookRepository.searchByNameOrIsbn(keyword, pageable);

        assertThat(resultPage).isEmpty();
        assertThat(resultPage.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("searchByNameOrIsbn should handle pagination correctly")
    void searchByNameOrIsbn_WithPagination_ShouldReturnCorrectSlice() {
        String keyword = "978-5"; // Этот префикс общего ISBN есть у обеих книг
        Pageable pageable = PageRequest.of(0, 1); // Страница 0, размер 1

        Page<Book> resultPage = bookRepository.searchByNameOrIsbn(keyword, pageable);

        assertThat(resultPage.getContent()).hasSize(1);
        assertThat(resultPage.getTotalElements()).isEqualTo(2); // Всего совпадений в БД
        assertThat(resultPage.getTotalPages()).isEqualTo(2);    // Всего страниц
    }

    @Test
    @DisplayName("findById should return book with all mapped relationships populated")
    void findById_WhenExists_ShouldReturnBookWithRelations() {
        Optional<Book> foundBook = bookRepository.findById(warAndPeace.getId());

        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getName()).isEqualTo("Война и мир");
        assertThat(foundBook.get().getAuthors()).isNotEmpty();
        assertThat(foundBook.get().getCategories()).isNotEmpty();
        assertThat(foundBook.get().getPublishers()).isNotEmpty();
    }

    @Test
    @DisplayName("save should persist new book and wire up references correctly")
    void save_ShouldPersistBook() {
        Book newBook = new Book("978-5-999-99999-9", "Руслан и Людмила", "Поэма Александра Пушкина.");

        Book savedBook = bookRepository.save(newBook);

        assertThat(savedBook.getId()).isNotNull();
        assertThat(bookRepository.findById(savedBook.getId())).isPresent();
    }

    @Test
    @DisplayName("deleteById should remove book from database but preserve independent справочники")
    void deleteById_WhenExists_ShouldRemoveBookOnly() {
        bookRepository.deleteById(warAndPeace.getId());
        Optional<Book> deletedBook = bookRepository.findById(warAndPeace.getId());

        assertThat(deletedBook).isEmpty();
        // Проверяем, что связанные сущности НЕ удалились из базы каскадом (они должны жить отдельно)
        assertThat(authorRepository.findAll()).isNotEmpty();
        assertThat(categoryRepository.findAll()).isNotEmpty();
    }

    @Test
    @DisplayName("Should successfully reload full bidirectional graph from database after session eviction")
    void save_ShouldSurviveSessionEvictionAndReloadGraphFromDatabase() {
        // 1. Создаем новые сущности-справочники через репозитории
        Author author = authorRepository.save(new Author("Martin Fowler", "Software architecture pioneer."));
        Category category = categoryRepository.save(new Category("Software Architecture"));
        Publisher publisher = publisherRepository.save(new Publisher("Addison-Wesley"));

        // 2. Создаем книгу и связываем объекты в памяти через утилитарные методы
        Book book = new Book("978-0-13-475759-9", "Refactoring", "Improving the Design of Existing Code.");
        book.addAuthor(author);
        book.addCategory(category);
        book.addPublisher(publisher);

        // 3. Сохраняем книгу в базу данных
        Book savedBook = bookRepository.save(book);
        Long bookId = savedBook.getId();

        // 4. Принудительно сбрасываем кэш Hibernate
        // flush() отправляет SQL-инструкции в базу PostgreSQL внутри контейнера
        // clear() полностью очищает сессию (Persistence Context) в памяти
        entityManager.flush();
        entityManager.clear();

        // 5. Загружаем книгу заново по ID (здесь сработает настроенный @EntityGraph)
        Optional<Book> foundBookOpt = bookRepository.findById(bookId);

        // 6. Проверяем, что книга и весь граф ее связей успешно поднялись из реальной БД
        assertThat(foundBookOpt).isPresent();
        Book foundBook = foundBookOpt.get();

        assertThat(foundBook.getName()).isEqualTo("Refactoring");
        assertThat(foundBook.getIsbn()).isEqualTo("978-0-13-475759-9");

        // Проверяем наличие связей с авторами, жанрами и издательствами после загрузки из базы
        assertThat(foundBook.getAuthors())
                .hasSize(1)
                .extracting(Author::getName)
                .containsExactly("Martin Fowler");

        assertThat(foundBook.getCategories())
                .hasSize(1)
                .extracting(Category::getName)
                .containsExactly("Software Architecture");

        assertThat(foundBook.getPublishers())
                .hasSize(1)
                .extracting(Publisher::getName)
                .containsExactly("Addison-Wesley");
    }
}