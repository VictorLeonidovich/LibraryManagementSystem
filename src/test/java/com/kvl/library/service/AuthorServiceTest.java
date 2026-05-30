package com.kvl.library.service;

import com.kvl.library.entity.Author;
import com.kvl.library.exception.EntityNotFoundException;
import com.kvl.library.repository.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorService Unit Tests")
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorService authorService;

    private Author testAuthor;
    private final Long authorId = 1L;

    @BeforeEach
    void setUp() {
        testAuthor = new Author();
        testAuthor.setId(authorId);
        testAuthor.setName("Лев Толстой");
        testAuthor.setDescription("Великий русский писатель, мыслитель и классик мировой литературы. " +
                "Автор романов «Война и мир» и «Анна Каренина». " +
                "Его философские идеи и глубокий психологизм оказали огромное влияние на мировую культуру.");
    }

    @Test
    @DisplayName("findAllAuthors should return list of authors")
    void findAllAuthors_ShouldReturnList() {
        List<Author> expectedAuthors = Collections.singletonList(testAuthor);
        when(authorRepository.findAll()).thenReturn(expectedAuthors);

        List<Author> actualAuthors = authorService.findAllAuthors();

        assertThat(actualAuthors).isNotEmpty().hasSize(1).contains(testAuthor);
        verify(authorRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findAuthorById should return author when found")
    void findAuthorById_WhenExists_ShouldReturnAuthor() {
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(testAuthor));

        Author actualAuthor = authorService.findAuthorById(authorId);

        assertThat(actualAuthor).isNotNull();
        assertThat(actualAuthor.getId()).isEqualTo(authorId);
        verify(authorRepository, times(1)).findById(authorId);
    }

    @Test
    @DisplayName("findAuthorById should throw EntityNotFoundException when not found")
    void findAuthorById_WhenNotFound_ShouldThrowException() {
        when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorService.findAuthorById(authorId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Author with ID " + authorId + " was not found");

        verify(authorRepository, times(1)).findById(authorId);
    }

    @Test
    @DisplayName("createAuthor should save author successfully")
    void createAuthor_ShouldSaveAuthor() {
        when(authorRepository.save(testAuthor)).thenReturn(testAuthor);

        authorService.createAuthor(testAuthor);

        verify(authorRepository, times(1)).save(testAuthor);
    }

    @Test
    @DisplayName("updateAuthor should save updated author successfully")
    void updateAuthor_ShouldSaveAuthor() {
        when(authorRepository.save(testAuthor)).thenReturn(testAuthor);

        authorService.updateAuthor(testAuthor);

        verify(authorRepository, times(1)).save(testAuthor);
    }

    @Test
    @DisplayName("deleteAuthor should delete author when exists")
    void deleteAuthor_WhenExists_ShouldDelete() {
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(testAuthor));
        doNothing().when(authorRepository).deleteById(authorId);

        authorService.deleteAuthor(authorId);

        verify(authorRepository, times(1)).findById(authorId);
        verify(authorRepository, times(1)).deleteById(authorId);
    }

    @Test
    @DisplayName("deleteAuthor should throw EntityNotFoundException and not delete when not found")
    void deleteAuthor_WhenNotFound_ShouldThrowException() {
        when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorService.deleteAuthor(authorId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Author with ID " + authorId + " was not found");

        verify(authorRepository, times(1)).findById(authorId);
        verify(authorRepository, never()).deleteById(anyLong());
    }
}