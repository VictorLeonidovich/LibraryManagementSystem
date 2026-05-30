package com.kvl.library.services;

import com.kvl.library.entity.Author;
import com.kvl.library.entity.Book;
import com.kvl.library.entity.Category;
import com.kvl.library.entity.Publisher;
import com.kvl.library.repositories.BooksRepository;
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
import org.springframework.data.domain.Sort;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BooksService Unit Tests")
class BooksServiceTest {

    @Mock
    private BooksRepository booksRepository;

    @InjectMocks
    private BooksService booksService;

    private Book testBook;
    private final Long bookId = 1L;

    @BeforeEach
    void setUp() {
        testBook = new Book("123-456", "Война и мир", "Роман. Классика.");
        testBook.setId(bookId);
    }

    @Test
    @DisplayName("findWithPaginationAndSorting() should return a sorted page descending when order is 'desc'")
    void findWithPaginationAndSorting_WhenOrderIsDesc_ShouldSortDescending() {
        Page<Book> expectedPage = new PageImpl<>(List.of(testBook));
        PageRequest expectedPageRequest = PageRequest.of(0, 10, Sort.by("name").descending());

        when(booksRepository.findAllWithAuthors(expectedPageRequest)).thenReturn(expectedPage);

        Page<Book> resultPage = booksService.findWithPaginationAndSorting(0, 10, "name", "desc");

        assertThat(resultPage).isEqualTo(expectedPage);
        verify(booksRepository, times(1)).findAllWithAuthors(expectedPageRequest);
    }

    @Test
    @DisplayName("findWithPaginationAndSorting() should return a sorted page ascending when order is 'asc' or any other value")
    void findWithPaginationAndSorting_WhenOrderIsAscOrOther_ShouldSortAscending() {
        Page<Book> expectedPage = new PageImpl<>(List.of(testBook));
        PageRequest expectedPageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());

        when(booksRepository.findAllWithAuthors(expectedPageRequest)).thenReturn(expectedPage);

        Page<Book> resultPage = booksService.findWithPaginationAndSorting(0, 10, "name", "asc");

        assertThat(resultPage).isEqualTo(expectedPage);
        verify(booksRepository, times(1)).findAllWithAuthors(expectedPageRequest);
    }

    @Test
    @DisplayName("findBookById() should return the expected book when it exists in the database")
    void findBookById_WhenBookExists_ShouldReturnBook() {
        when(booksRepository.findById(bookId)).thenReturn(Optional.of(testBook));

        Book resultBook = booksService.findBookById(bookId);

        assertThat(resultBook).isEqualTo(testBook);
        verify(booksRepository, times(1)).findById(bookId);
    }

    @Test
    @DisplayName("findBookById() should throw RuntimeException when the requested book ID does not exist")
    void findBookById_WhenBookDoesNotExist_ShouldThrowRuntimeException() {
        when(booksRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> booksService.findBookById(bookId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Book is not found");

        verify(booksRepository, times(1)).findById(bookId);
    }

    @Test
    @DisplayName("createBook() should successfully invoke the repository save function to create a book")
    void createBook_ShouldSaveBookSuccessfully() {
        booksService.createBook(testBook);

        verify(booksRepository, times(1)).save(testBook);
    }

    @Test
    @DisplayName("updateBook() should successfully update basic fields and clear/populate relation sets when book exists")
    void updateBook_WhenBookExistsAndHasRelationships_ShouldUpdateAndSyncCollections() {
        Book managedBook = spy(new Book("000-000", "Old Name", "Old Description"));
        managedBook.setId(bookId);

        String isbn = "789-000";
        String newName = "New Name";
        String newDescription = "New Description";
        Book updateDetails = new Book(isbn, newName, newDescription);
        updateDetails.setId(bookId);

        Author author = new Author();
        Category category = new Category();
        Publisher publisher = new Publisher();

        updateDetails.setAuthors(new HashSet<>(Set.of(author)));
        updateDetails.setCategories(new HashSet<>(Set.of(category)));
        updateDetails.setPublishers(new HashSet<>(Set.of(publisher)));

        // Using lenient() prevents UnnecessaryStubbingException caused by object spys or log tracing evaluation
        lenient().when(booksRepository.findById(bookId)).thenReturn(Optional.of(managedBook));

        booksService.updateBook(updateDetails);

        assertThat(managedBook.getName()).isEqualTo(newName);
        assertThat(managedBook.getIsbn()).isEqualTo(isbn);
        assertThat(managedBook.getDescription()).isEqualTo(newDescription);
        assertThat(managedBook.getAuthors()).containsExactly(author);
        assertThat(managedBook.getCategories()).containsExactly(category);
        assertThat(managedBook.getPublishers()).containsExactly(publisher);

        // REMOVED: verify save() because your method relies on Spring @Transactional dirty checking
        //verify(booksRepository, times(1)).save(managedBook);
    }

    @Test
    @DisplayName("updateBook() should safely update fields without interacting with sets when relationship items are null")
    void updateBook_WhenRelationshipsAreNull_ShouldNotClearOrUpdateCollections() {
        Book managedBook = spy(new Book("123-456", "Original Name", "Original Desc"));
        managedBook.setId(bookId);

        Book updateDetails = new Book("123-456", "Changed Name", "Original Desc");
        updateDetails.setId(bookId);
        updateDetails.setAuthors(null);
        updateDetails.setCategories(null);
        updateDetails.setPublishers(null);

        // Using lenient() prevents UnnecessaryStubbingException caused by object spys or log tracing evaluation
        lenient().when(booksRepository.findById(bookId)).thenReturn(Optional.of(managedBook));

        booksService.updateBook(updateDetails);

        verify(managedBook, never()).getAuthors();
        verify(managedBook, never()).getCategories();
        verify(managedBook, never()).getPublishers();

        // REMOVED: verify save() because your method relies on Spring @Transactional dirty checking
        //verify(booksRepository, times(1)).save(managedBook);
    }

    @Test
    @DisplayName("updateBook() should throw RuntimeException when processing an update details entity missing an ID")
    void updateBook_WhenBookIdMissing_ShouldThrowRuntimeException() {
        Book bookWithoutId = new Book("123-456", "No ID Book", "Description");
        bookWithoutId.setId(null);

        assertThatThrownBy(() -> booksService.updateBook(bookWithoutId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cannot update book: ID is missing");

        verify(booksRepository, never()).findById(anyLong());
        verify(booksRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("updateBook() should throw RuntimeException when target entity ID does not exist in the database")
    void updateBook_WhenBookDoesNotExistInDatabase_ShouldThrowRuntimeException() {
        when(booksRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> booksService.updateBook(testBook))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cannot update book: entity does not exist");

        verify(booksRepository, times(1)).findById(bookId);
        verify(booksRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("deleteBook() should remove the book from the database when it exists")
    void deleteBook_WhenBookExists_ShouldDeleteBook() {
        when(booksRepository.findById(bookId)).thenReturn(Optional.of(testBook));

        booksService.deleteBook(bookId);

        verify(booksRepository, times(1)).findById(bookId);
        verify(booksRepository, times(1)).delete(testBook);
    }

    @Test
    @DisplayName("deleteBook() should throw RuntimeException and skip deletion routines if the book ID is missing from database")
    void deleteBook_WhenBookDoesNotExist_ShouldThrowRuntimeException() {
        when(booksRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> booksService.deleteBook(bookId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Book is not found");

        verify(booksRepository, times(1)).findById(bookId);
        verify(booksRepository, never()).delete(any(Book.class));
    }
}