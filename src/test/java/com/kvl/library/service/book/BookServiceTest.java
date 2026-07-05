package com.kvl.library.service.book;

import com.kvl.library.entity.Book;
import com.kvl.library.exception.EntityNotFoundException;
import com.kvl.library.repository.BookRepository;
import com.kvl.library.service.book.impl.BookServiceImpl;
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

    @Mock
    private BookPopularityService bookPopularityService;

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
    @DisplayName("findBookById() should return the expected book and trigger popularity increment")
    void findBookById_WhenBookExists_ShouldReturnBookAndIncrementView() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(testBook));
        doNothing().when(bookPopularityService).incrementView(testBook.getIsbn());

        Book actualBook = bookService.findBookById(bookId);

        assertThat(actualBook).isEqualTo(testBook);
        verify(bookRepository, times(1)).findById(bookId);
        // Проверяем, что метод инкремента просмотров был успешно вызван у нового сервиса
        verify(bookPopularityService, times(1)).incrementView(testBook.getIsbn());
    }

    @Test
    @DisplayName("findBookById() should throw EntityNotFoundException when the requested book ID does not exist")
    void findBookById_WhenBookDoesNotExist_ShouldThrowEntityNotFoundException() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.findBookById(bookId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with ID " + bookId + " was not found");

        verify(bookRepository, times(1)).findById(bookId);
        verifyNoInteractions(bookPopularityService);
    }

    @Test
    @DisplayName("createBook() should successfully invoke the repository save function to create a book")
    void createBook_ShouldSaveBook() {
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        bookService.createBook(testBook);

        verify(bookRepository, times(1)).save(testBook);
    }

    @Test
    @DisplayName("updateBook() should successfully mutate and save updated book when it exists")
    void updateBook_WhenBookExists_ShouldSaveBook() {
        // Подготавливаем существующую в БД книгу (старое состояние)
        Book existingBook = new Book();
        existingBook.setId(bookId);
        existingBook.setName("Старое название");
        existingBook.setIsbn("111-1-111-11111-1");
        existingBook.setDescription("Старое описание");

        // Подготавливаем новые данные для обновления (из веб-формы / DTO)
        Book updatedData = new Book();
        updatedData.setId(bookId);
        updatedData.setName("Война и мир");
        updatedData.setIsbn("978-5-699-12345-6");
        updatedData.setDescription("Исторический роман-эпопея.");

        // Мокаем цепочку: находим старую книгу, сохраняем обновленную
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenReturn(existingBook);

        // Запуск бизнес-логики
        bookService.updateBook(updatedData);

        // Проверяем, что метод findById был вызван вместо старого existsById
        verify(bookRepository, times(1)).findById(bookId);

        // Проверяем, что поля старого управляемого объекта корректно мутировали в новые значения
        assertThat(existingBook.getName()).isEqualTo("Война и мир");
        assertThat(existingBook.getIsbn()).isEqualTo("978-5-699-12345-6");
        assertThat(existingBook.getDescription()).isEqualTo("Исторический роман-эпопея.");

        verify(bookRepository, times(1)).save(existingBook);
    }

    @Test
    @DisplayName("updateBook() should throw EntityNotFoundException when book does not exist in database")
    void updateBook_WhenBookDoesNotExist_ShouldThrowException() {
        // Если книга не найдена в базе при обновлении
        when(bookRepository.findById(testBook.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(testBook))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with ID " + testBook.getId() + " was not found");

        // Проверяем, что безусловно вылетает ошибка и сохранения не происходит
        verify(bookRepository, times(1)).findById(testBook.getId());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("deleteBook() should remove the book from database and exclude it from popularity chart")
    void deleteBook_WhenBookExists_ShouldDeleteBookAndRemoveFromPopularity() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(testBook));
        doNothing().when(bookPopularityService).removeBook(testBook.getIsbn());
        doNothing().when(bookRepository).deleteById(bookId);

        bookService.deleteBook(bookId);

        verify(bookRepository, times(1)).findById(bookId);
        // Проверяем, что метод исключения из чарта популярности был вызван у нового сервиса
        verify(bookPopularityService, times(1)).removeBook(testBook.getIsbn());
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
        verifyNoInteractions(bookPopularityService);
        verify(bookRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("findPopularBookIsbns() should return top list from popularity service when cache misses")
    void findPopularBookIsbns_WhenPopularityServiceHasData_ShouldReturnTopList() {
        List<String> expectedIsbns = List.of("978-5-699-12345-6");
        when(bookPopularityService.getTopBooks(10)).thenReturn(expectedIsbns);

        List<String> actualIsbns = bookService.findPopularBookIsbns();

        assertThat(actualIsbns).containsExactlyElementsOf(expectedIsbns);
        verify(bookPopularityService, times(1)).getTopBooks(10);
        verifyNoInteractions(bookRepository);
    }

    @Test
    @DisplayName("findPopularBookIsbns() should fallback to repository when popularity service returns empty result")
    void findPopularBookIsbns_WhenPopularityServiceIsEmpty_ShouldFallbackToRepository() {
        List<String> fallbackIsbns = List.of("978-5-699-12345-6");
        when(bookPopularityService.getTopBooks(10)).thenReturn(Collections.emptyList());
        when(bookRepository.findTop10Isbns(any(PageRequest.class))).thenReturn(fallbackIsbns);

        List<String> actualIsbns = bookService.findPopularBookIsbns();

        assertThat(actualIsbns).containsExactlyElementsOf(fallbackIsbns);
        verify(bookPopularityService, times(1)).getTopBooks(10);
        verify(bookRepository, times(1)).findTop10Isbns(any(PageRequest.class));
    }
}