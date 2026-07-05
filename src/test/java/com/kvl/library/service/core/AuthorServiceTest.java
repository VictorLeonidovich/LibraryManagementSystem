package com.kvl.library.service.core;

import com.kvl.library.entity.Author;
import com.kvl.library.exception.EntityNotFoundException;
import com.kvl.library.repository.AuthorRepository;
import com.kvl.library.service.core.impl.AuthorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    private AuthorServiceImpl authorService;

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
    @DisplayName("findAllAuthors should return page of authors")
    void findAllAuthors_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Author> authorList = Collections.singletonList(testAuthor);
        Page<Author> expectedPage = new PageImpl<>(authorList, pageable, authorList.size());

        when(authorRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<Author> actualPage = authorService.findAllAuthors(pageable);

        assertThat(actualPage).isNotEmpty();
        assertThat(actualPage.getTotalElements()).isEqualTo(1);
        assertThat(actualPage.getContent()).contains(testAuthor);
        verify(authorRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("searchAuthorsByName should return page of filtered authors")
    void searchAuthorsByName_ShouldReturnFilteredPage() {
        String searchName = "Толстой";
        Pageable pageable = PageRequest.of(0, 10);
        List<Author> authorList = Collections.singletonList(testAuthor);
        Page<Author> expectedPage = new PageImpl<>(authorList, pageable, authorList.size());

        when(authorRepository.findByNameContainingIgnoreCase(searchName, pageable)).thenReturn(expectedPage);

        Page<Author> actualPage = authorService.searchAuthorsByName(searchName, pageable);

        assertThat(actualPage).isNotEmpty();
        assertThat(actualPage.getContent().get(0).getName()).contains(searchName);
        verify(authorRepository, times(1)).findByNameContainingIgnoreCase(searchName, pageable);
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
    @DisplayName("updateAuthor should save updated author when author exists")
    void updateAuthor_WhenExists_ShouldSaveAuthor() {
        when(authorRepository.existsById(testAuthor.getId())).thenReturn(true);
        when(authorRepository.save(testAuthor)).thenReturn(testAuthor);

        authorService.updateAuthor(testAuthor);

        verify(authorRepository, times(1)).existsById(testAuthor.getId());
        verify(authorRepository, times(1)).save(testAuthor);
    }

    @Test
    @DisplayName("updateAuthor should throw EntityNotFoundException when author does not exist")
    void updateAuthor_WhenNotExists_ShouldThrowException() {
        when(authorRepository.existsById(testAuthor.getId())).thenReturn(false);

        assertThatThrownBy(() -> authorService.updateAuthor(testAuthor))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Author with ID " + testAuthor.getId() + " was not found");

        verify(authorRepository, times(1)).existsById(testAuthor.getId());
        verify(authorRepository, never()).save(any(Author.class));
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