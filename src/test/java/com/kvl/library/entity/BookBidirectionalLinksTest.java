package com.kvl.library.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Book Entity Bidirectional Links Tests")
class BookBidirectionalLinksTest {

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book("978-3-16-148410-0", "Effective Java", "Programming Guide");
    }

    @Nested
    @DisplayName("Author Association Management")
    class AuthorAssociationTests {

        private Author author;

        @BeforeEach
        void setUp() {
            author = new Author("Joshua Bloch", "Java Guru");
        }

        @Test
        @DisplayName("Should add author to book and book to author simultaneously")
        void shouldSynchronizeGraphWhenAuthorAdded() {
            book.addAuthor(author);

            // Проверяем синхронизацию графа объектов в обе стороны
            assertThat(book.getAuthors()).containsExactly(author);
            assertThat(author.getBooks()).containsExactly(book);
        }

        @Test
        @DisplayName("Should remove author from book and book from author simultaneously")
        void shouldSynchronizeGraphWhenAuthorRemoved() {
            book.addAuthor(author);
            book.removeAuthor(author);

            // Проверяем полную очистку связей в оперативной памяти
            assertThat(book.getAuthors()).isEmpty();
            assertThat(author.getBooks()).isEmpty();
        }

        @Test
        @DisplayName("Should handle null safely without throwing exception")
        void shouldNotThrowWhenAuthorIsNull() {
            // Проверяем предохранитель от NullPointerException
            book.removeAuthor(null);
            assertThat(book.getAuthors()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Category Association Management")
    class CategoryAssociationTests {

        private Category category;

        @BeforeEach
        void setUp() {
            category = new Category("Programming");
        }

        @Test
        @DisplayName("Should add category to book and book to category simultaneously")
        void shouldSynchronizeGraphWhenCategoryAdded() {
            book.addCategory(category);

            // Проверяем синхронизацию графа объектов в обе стороны
            assertThat(book.getCategories()).containsExactly(category);
            assertThat(category.getBooks()).containsExactly(book);
        }

        @Test
        @DisplayName("Should remove category from book and book from category simultaneously")
        void shouldSynchronizeGraphWhenCategoryRemoved() {
            book.addCategory(category);
            book.removeCategory(category);

            // Проверяем полную очистку связей в оперативной памяти
            assertThat(book.getCategories()).isEmpty();
            assertThat(category.getBooks()).isEmpty();
        }

        @Test
        @DisplayName("Should handle null safely without throwing exception")
        void shouldNotThrowWhenCategoryIsNull() {
            // Проверяем предохранитель от NullPointerException
            book.removeCategory(null);
            assertThat(book.getCategories()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Publisher Association Management")
    class PublisherAssociationTests {

        private Publisher publisher;

        @BeforeEach
        void setUp() {
            publisher = new Publisher("Addison-Wesley");
        }

        @Test
        @DisplayName("Should add publisher to book and book to publisher simultaneously")
        void shouldSynchronizeGraphWhenPublisherAdded() {
            book.addPublisher(publisher);

            // Проверяем синхронизацию графа объектов в обе стороны
            assertThat(book.getPublishers()).containsExactly(publisher);
            assertThat(publisher.getBooks()).containsExactly(book);
        }

        @Test
        @DisplayName("Should remove publisher from book and book from publisher simultaneously")
        void shouldSynchronizeGraphWhenPublisherRemoved() {
            book.addPublisher(publisher);
            book.removePublisher(publisher);

            // Проверяем полную очистку связей в оперативной памяти
            assertThat(book.getPublishers()).isEmpty();
            assertThat(publisher.getBooks()).isEmpty();
        }

        @Test
        @DisplayName("Should handle null safely without throwing exception")
        void shouldNotThrowWhenPublisherIsNull() {
            // Проверяем предохранитель от NullPointerException
            book.removePublisher(null);
            assertThat(book.getPublishers()).isEmpty();
        }
    }
}