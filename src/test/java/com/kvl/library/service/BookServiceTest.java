package com.kvl.library.service;


import com.kvl.library.entity.Book;
import com.kvl.library.exception.EntityNotFoundException;
import com.kvl.library.repository.BookRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("deprecation")
@Deprecated
@DisplayName("BookService Unit Tests")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book testBook;
    private final Long bookId = 1L;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(bookId);
    }

    @Test
    @DisplayName("findAllBooks() should return a list containing all books found in the database")
    void findAllBooks_ShouldReturnListOfBooks() {
        List<Book> expectedBooks = List.of(testBook);
        when(bookRepository.findAll()).thenReturn(expectedBooks);

        List<Book> actualBooks = bookService.findAllBooks();

        assertThat(actualBooks).hasSize(1).containsExactly(testBook);
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findAllBooks() should return an empty list when no books exist in the database")
    void findAllBooks_WhenEmpty_ShouldReturnEmptyList() {
        when(bookRepository.findAll()).thenReturn(Collections.emptyList());

        List<Book> actualBooks = bookService.findAllBooks();

        assertThat(actualBooks).isEmpty();
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findBookById() should return the expected book when it exists in the database")
    void findBookById_WhenBookExists_ShouldReturnBook() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(testBook));

        Book actualBook = bookService.findBookById(bookId);

        assertThat(actualBook).isEqualTo(testBook);
        verify(bookRepository, times(1)).findById(bookId);
    }

    @Test
    @DisplayName("findBookById() should throw EntityNotFoundException when the requested book ID does not exist")
    void findBookById_WhenBookDoesNotExist_ShouldThrowEntityNotFoundException() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.findBookById(bookId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with ID " + bookId + " was not found");

        verify(bookRepository, times(1)).findById(bookId);
    }

    @Test
    @DisplayName("createBook() should successfully invoke the repository save function to create a book")
    void createBook_ShouldSaveBook() {
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        bookService.createBook(testBook);

        verify(bookRepository, times(1)).save(testBook);
    }

    @Test
    @DisplayName("updateBook() should successfully invoke the repository save function to update a book profile")
    void updateBook_ShouldSaveBookDirectly() {
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        bookService.updateBook(testBook);

        verify(bookRepository, times(1)).save(testBook);
    }

    @Test
    @DisplayName("deleteBook() should remove the book from the database when it exists")
    void deleteBook_WhenBookExists_ShouldDeleteBook() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(testBook));
        doNothing().when(bookRepository).deleteById(bookId);

        bookService.deleteBook(bookId);

        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).deleteById(bookId);
    }

    @Test
    @DisplayName("deleteBook() should throw EntityNotFoundException and skip deletion routines if the book ID is missing")
    void deleteBook_WhenBookDoesNotExist_ShouldThrowEntityNotFoundException() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.deleteBook(bookId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with ID " + bookId + " was not found");

        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, never()).deleteById(anyLong());
    }
}