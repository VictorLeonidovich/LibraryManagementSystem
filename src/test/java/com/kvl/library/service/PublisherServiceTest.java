package com.kvl.library.service;

import com.kvl.library.entity.Publisher;
import com.kvl.library.exception.EntityNotFoundException;
import com.kvl.library.repository.PublisherRepository;
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
@DisplayName("PublisherService Unit Tests")
class PublisherServiceTest {

    public static final String TEST_NAME = "Молодая гвардия";

    @Mock
    private PublisherRepository publisherRepository;

    @InjectMocks
    private PublisherService publisherService;

    private Publisher testPublisher;
    private final Long publisherId = 1L;

    @BeforeEach
    void setUp() {
        testPublisher = new Publisher();
        testPublisher.setId(publisherId);
        testPublisher.setName(TEST_NAME);
    }

    @Test
    @DisplayName("findAllPublishers without parameters should return list of publishers")
    void findAllPublishers_WithoutParameters_ShouldReturnList() {
        List<Publisher> expectedPublishers = Collections.singletonList(testPublisher);
        when(publisherRepository.findAll()).thenReturn(expectedPublishers);

        List<Publisher> actualPublishers = publisherService.findAllPublishers();

        assertThat(actualPublishers).isNotEmpty().hasSize(1).contains(testPublisher);
        verify(publisherRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findAllPublishers with pageable should return page of publishers")
    void findAllPublishers_WithPageable_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Publisher> publisherList = Collections.singletonList(testPublisher);
        Page<Publisher> expectedPage = new PageImpl<>(publisherList, pageable, publisherList.size());

        when(publisherRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<Publisher> actualPage = publisherService.findAllPublishers(pageable);

        assertThat(actualPage).isNotEmpty();
        assertThat(actualPage.getTotalElements()).isEqualTo(1);
        assertThat(actualPage.getContent()).contains(testPublisher);
        verify(publisherRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("searchPublishersByName should return page of filtered publishers")
    void searchPublishersByName_ShouldReturnFilteredPage() {
        String searchName = "Молодая";
        Pageable pageable = PageRequest.of(0, 10);
        List<Publisher> publisherList = Collections.singletonList(testPublisher);
        Page<Publisher> expectedPage = new PageImpl<>(publisherList, pageable, publisherList.size());

        when(publisherRepository.findByNameContainingIgnoreCase(searchName, pageable)).thenReturn(expectedPage);

        Page<Publisher> actualPage = publisherService.searchPublishersByName(searchName, pageable);

        assertThat(actualPage).isNotEmpty();
        assertThat(actualPage.getContent().get(0).getName()).contains(searchName);
        verify(publisherRepository, times(1)).findByNameContainingIgnoreCase(searchName, pageable);
    }

    @Test
    @DisplayName("findPublisherById should return publisher when found")
    void findPublisherById_WhenExists_ShouldReturnPublisher() {
        when(publisherRepository.findById(publisherId)).thenReturn(Optional.of(testPublisher));

        Publisher actualPublisher = publisherService.findPublisherById(publisherId);

        assertThat(actualPublisher).isNotNull();
        assertThat(actualPublisher.getId()).isEqualTo(publisherId);
        assertThat(actualPublisher.getName()).isEqualTo(TEST_NAME);
        verify(publisherRepository, times(1)).findById(publisherId);
    }

    @Test
    @DisplayName("findPublisherById should throw EntityNotFoundException when not found")
    void findPublisherById_WhenNotFound_ShouldThrowException() {
        when(publisherRepository.findById(publisherId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> publisherService.findPublisherById(publisherId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Publisher with ID " + publisherId + " was not found");

        verify(publisherRepository, times(1)).findById(publisherId);
    }

    @Test
    @DisplayName("createPublisher should save publisher successfully")
    void createPublisher_ShouldSavePublisher() {
        when(publisherRepository.save(testPublisher)).thenReturn(testPublisher);

        publisherService.createPublisher(testPublisher);

        verify(publisherRepository, times(1)).save(testPublisher);
    }

    @Test
    @DisplayName("updatePublisher should save updated publisher when publisher exists")
    void updatePublisher_WhenExists_ShouldSavePublisher() {
        when(publisherRepository.existsById(testPublisher.getId())).thenReturn(true);
        when(publisherRepository.save(testPublisher)).thenReturn(testPublisher);

        publisherService.updatePublisher(testPublisher);

        verify(publisherRepository, times(1)).existsById(testPublisher.getId());
        verify(publisherRepository, times(1)).save(testPublisher);
    }

    @Test
    @DisplayName("updatePublisher should throw EntityNotFoundException when publisher does not exist")
    void updatePublisher_WhenNotExists_ShouldThrowException() {
        when(publisherRepository.existsById(testPublisher.getId())).thenReturn(false);

        assertThatThrownBy(() -> publisherService.updatePublisher(testPublisher))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Publisher with ID " + testPublisher.getId() + " was not found");

        verify(publisherRepository, times(1)).existsById(testPublisher.getId());
        verify(publisherRepository, never()).save(any(Publisher.class));
    }

    @Test
    @DisplayName("deletePublisher should delete publisher when exists")
    void deletePublisher_WhenExists_ShouldDelete() {
        when(publisherRepository.findById(publisherId)).thenReturn(Optional.of(testPublisher));
        doNothing().when(publisherRepository).deleteById(publisherId);

        publisherService.deletePublisher(publisherId);

        verify(publisherRepository, times(1)).findById(publisherId);
        verify(publisherRepository, times(1)).deleteById(publisherId);
    }

    @Test
    @DisplayName("deletePublisher should throw EntityNotFoundException and not delete when not found")
    void deletePublisher_WhenNotFound_ShouldThrowException() {
        when(publisherRepository.findById(publisherId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> publisherService.deletePublisher(publisherId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Publisher with ID " + publisherId + " was not found");

        verify(publisherRepository, times(1)).findById(publisherId);
        verify(publisherRepository, never()).deleteById(anyLong());
    }
}