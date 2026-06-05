package com.kvl.library.mapper;

import com.kvl.library.dto.PublisherRequestDTO;
import com.kvl.library.dto.PublisherResponseDTO;
import com.kvl.library.entity.Publisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PublisherMapper Unit Tests")
class PublisherMapperTest {

    // Manually instantiate the compiled MapStruct implementation class
    private final PublisherMapper publisherMapper = new PublisherMapperImpl();

    @Test
    @DisplayName("toResponseDTO should map Publisher entity to PublisherResponseDTO correctly")
    void toResponseDTO_ShouldMapEntityToResponseDTO() {
        Publisher publisher = new Publisher("Эксмо");
        publisher.setId(15L);

        PublisherResponseDTO responseDTO = publisherMapper.toResponseDTO(publisher);

        assertThat(responseDTO).isNotNull();
        assertThat(responseDTO.getId()).isEqualTo(publisher.getId());
        assertThat(responseDTO.getName()).isEqualTo(publisher.getName());
    }

    @Test
    @DisplayName("toEntity should map PublisherRequestDTO to Publisher entity and ignore id and books")
    void toEntity_ShouldMapRequestDTOToEntityAndIgnoreFields() {
        PublisherRequestDTO requestDTO = new PublisherRequestDTO();
        requestDTO.setName("Просвещение");

        Publisher publisher = publisherMapper.toEntity(requestDTO);

        assertThat(publisher).isNotNull();
        assertThat(publisher.getId()).isNull();          // Verifies @Mapping(target = "id", ignore = true)
        assertThat(publisher.getBooks()).isEmpty();       // Verifies @Mapping(target = "books", ignore = true)
        assertThat(publisher.getName()).isEqualTo(requestDTO.getName());
    }

    @Test
    @DisplayName("updateEntityFromDto should modify existing entity fields and preserve its id and books relationship")
    void updateEntityFromDto_ShouldModifyExistingEntityAndPreserveId() {
        Publisher existingPublisher = new Publisher("Старое название");
        existingPublisher.setId(55L);

        PublisherRequestDTO updateDTO = new PublisherRequestDTO();
        updateDTO.setName("Новое название");

        publisherMapper.updateEntityFromDto(updateDTO, existingPublisher);

        assertThat(existingPublisher.getId()).isEqualTo(55L); // ID must remain untouched
        assertThat(existingPublisher.getName()).isEqualTo("Новое название"); // Name updated correctly
        assertThat(existingPublisher.getBooks()).isEmpty(); // Collection is not overridden with null
    }

    @Test
    @DisplayName("toResponseDTO should return null when source entity is null")
    void toResponseDTO_WhenSourceIsNull_ShouldReturnNull() {
        assertThat(publisherMapper.toResponseDTO(null)).isNull();
    }

    @Test
    @DisplayName("toEntity should return null when source DTO is null")
    void toEntity_WhenSourceIsNull_ShouldReturnNull() {
        assertThat(publisherMapper.toEntity(null)).isNull();
    }
}