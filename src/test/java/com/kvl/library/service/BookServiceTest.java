package com.kvl.library.service;


import com.kvl.library.entity.Book;
import com.kvl.library.exception.EntityNotFoundException;
import com.kvl.library.repository.BookRepository;
import com.kvl.library.service.impl.BookServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Unit Tests")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book testBook;
    private final Long bookId = 1L;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(bookId);
        testBook.setName("Война и мир");
        testBook.setIsbn("978-5-699-12345-6");
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
    @DisplayName("findAllBooks with pageable should return page of books")
    void findAllBooks_WithPageable_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> bookList = Collections.singletonList(testBook);
        Page<Book> expectedPage = new PageImpl<>(bookList, pageable, bookList.size());

        when(bookRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<Book> actualPage = bookService.findAllBooks(pageable);

        assertThat(actualPage).isNotEmpty();
        assertThat(actualPage.getTotalElements()).isEqualTo(1);
        assertThat(actualPage.getContent()).contains(testBook);
        verify(bookRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("searchBooks should return page of filtered books by name or isbn")
    void searchBooks_ShouldReturnFilteredPage() {
        String keyword = "Война";
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> bookList = Collections.singletonList(testBook);
        Page<Book> expectedPage = new PageImpl<>(bookList, pageable, bookList.size());

        when(bookRepository.searchByNameOrIsbn(keyword, pageable)).thenReturn(expectedPage);

        Page<Book> actualPage = bookService.searchBooks(keyword, pageable);

        assertThat(actualPage).isNotEmpty();
        assertThat(actualPage.getContent().get(0).getName()).contains(keyword);
        verify(bookRepository, times(1)).searchByNameOrIsbn(keyword, pageable);
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
    @DisplayName("updateBook() should successfully save updated book when it exists")
    void updateBook_WhenBookExists_ShouldSaveBook() {
        when(bookRepository.existsById(testBook.getId())).thenReturn(true);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        bookService.updateBook(testBook);

        verify(bookRepository, times(1)).existsById(testBook.getId());
        verify(bookRepository, times(1)).save(testBook);
    }

    @Test
    @DisplayName("updateBook() should throw EntityNotFoundException when book does not exist")
    void updateBook_WhenBookDoesNotExist_ShouldThrowException() {
        when(bookRepository.existsById(testBook.getId())).thenReturn(false);

        assertThatThrownBy(() -> bookService.updateBook(testBook))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with ID " + testBook.getId() + " was not found");

        verify(bookRepository, times(1)).existsById(testBook.getId());
        verify(bookRepository, never()).save(any(Book.class));
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