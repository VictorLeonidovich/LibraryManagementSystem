package com.kvl.library.mapper;

import com.kvl.library.dto.BookRequestDTO;
import com.kvl.library.dto.BookResponseDTO;
import com.kvl.library.entity.Author;
import com.kvl.library.entity.Book;
import com.kvl.library.entity.Category;
import com.kvl.library.entity.Publisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BookMapper Unit Tests")
class BookMapperTest {

    // Instantiate BookMapperImpl manually.
    // Since it "uses" other mappers, MapStruct injects them as component references.
    private final BookMapper bookMapper = new BookMapperImpl();

    public BookMapperTest() {
        // MapStruct generates fields inside BookMapperImpl for its child mappers.
        // We inject them manually using ReflectionTestUtils so it doesn't throw NullPointerException.
        ReflectionTestUtils.setField(bookMapper, "authorMapper", new AuthorMapperImpl());
        ReflectionTestUtils.setField(bookMapper, "categoryMapper", new CategoryMapperImpl());
        ReflectionTestUtils.setField(bookMapper, "publisherMapper", new PublisherMapperImpl());
    }

    @Test
    @DisplayName("toResponseDTO should map Book entity and its nested relational collections correctly")
    void toResponseDTO_ShouldMapEntityAndNestedCollections() {
        Book book = new Book("978-5-699-12345-6", "Война и мир", "Исторический роман-эпопея.");
        book.setId(101L);

        Author author = new Author("Лев Толстой", "Классик мировой литературы.");
        author.setId(1L);
        book.addAuthor(author);

        Category category = new Category("Художественная литература");
        category.setId(2L);
        book.addCategory(category);

        Publisher publisher = new Publisher("Эксмо");
        publisher.setId(3L);
        book.addPublisher(publisher);

        BookResponseDTO responseDTO = bookMapper.toResponseDTO(book);

        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getId()).isEqualTo(101L);
        assertThat(responseDTO.getIsbn()).isEqualTo("978-5-699-12345-6");
        assertThat(responseDTO.getName()).isEqualTo("Война и мир");
        assertThat(responseDTO.getDescription()).isEqualTo("Исторический роман-эпопея.");

        // Assert nested components are correctly delegated and mapped
        assertThat(responseDTO.getAuthors()).hasSize(1);
        assertThat(responseDTO.getAuthors().iterator().next().getName()).isEqualTo("Лев Толстой");

        assertThat(responseDTO.getCategories()).hasSize(1);
        assertThat(responseDTO.getCategories().iterator().next().getName()).isEqualTo("Художественная литература");

        assertThat(responseDTO.getPublishers()).hasSize(1);
        assertThat(responseDTO.getPublishers().iterator().next().getName()).isEqualTo("Эксмо");
    }

    @Test
    @DisplayName("toEntity should map BookRequestDTO text attributes and strictly ignore complex relational collections")
    void toEntity_ShouldMapRequestDTOAndIgnoreComplexCollections() {
        BookRequestDTO requestDTO = new BookRequestDTO();
        requestDTO.setIsbn("978-5-699-54321-0");
        requestDTO.setName("Евгений Онегин");
        requestDTO.setDescription("Роман в стихах.");
        requestDTO.setAuthorIds(Set.of(1L));
        requestDTO.setCategoryIds(Set.of(2L));
        requestDTO.setPublisherIds(Set.of(3L));

        Book book = bookMapper.toEntity(requestDTO);

        assertThat(book).isNotNull();
        assertThat(book.getId()).isNull(); // Verifies @Mapping(target = "id", ignore = true)
        assertThat(book.getIsbn()).isEqualTo("978-5-699-54321-0");
        assertThat(book.getName()).isEqualTo("Евгений Онегин");
        assertThat(book.getDescription()).isEqualTo("Роман в стихах.");

        // Relational structural collections must be left empty for Service-layer resolving
        assertThat(book.getAuthors()).isEmpty();     // Verifies @Mapping(target = "authors", ignore = true)
        assertThat(book.getCategories()).isEmpty();  // Verifies @Mapping(target = "categories", ignore = true)
        assertThat(book.getPublishers()).isEmpty();  // Verifies @Mapping(target = "publishers", ignore = true)
    }

    @Test
    @DisplayName("updateEntityFromDto should alter core book parameters and preserve entity ID and complex relations")
    void updateEntityFromDto_ShouldModifyCoreParametersAndPreserveRelations() {
        Book existingBook = new Book("978-5-000-00000-0", "Старое название", "Старое описание");
        existingBook.setId(999L);

        Author preservedAuthor = new Author("Лев Толстой", "Описание");
        existingBook.addAuthor(preservedAuthor);

        BookRequestDTO updateDTO = new BookRequestDTO();
        updateDTO.setIsbn("978-5-111-11111-1");
        updateDTO.setName("Новое название");
        updateDTO.setDescription("Новое описание");

        bookMapper.updateEntityFromDto(updateDTO, existingBook);

        assertThat(existingBook.getId()).isEqualTo(999L); // ID must never change
        assertThat(existingBook.getIsbn()).isEqualTo("978-5-111-11111-1");
        assertThat(existingBook.getName()).isEqualTo("Новое название");
        assertThat(existingBook.getDescription()).isEqualTo("Новое описание");

        // Relation objects must not be replaced, deleted, or cleared out by the mapping framework
        assertThat(existingBook.getAuthors()).containsExactly(preservedAuthor);
    }

    @Test
    @DisplayName("toResponseDTO should return null when source book is null")
    void toResponseDTO_WhenSourceIsNull_ShouldReturnNull() {
        assertThat(bookMapper.toResponseDTO(null)).isNull();
    }

    @Test
    @DisplayName("toEntity should return null when source DTO is null")
    void toEntity_WhenSourceIsNull_ShouldReturnNull() {
        assertThat(bookMapper.toEntity(null)).isNull();
    }
}