package com.mehaj.flightcrew.service;

import com.mehaj.flightcrew.dto.AircraftCreateRequest;
import com.mehaj.flightcrew.dto.AircraftResponse;
import com.mehaj.flightcrew.dto.AircraftUpdateRequest;
import com.mehaj.flightcrew.entity.Aircraft;
import com.mehaj.flightcrew.entity.AircraftStatus;
import com.mehaj.flightcrew.exception.DuplicateResourceException;
import com.mehaj.flightcrew.exception.ResourceNotFoundException;
import com.mehaj.flightcrew.mapper.AircraftMapper;
import com.mehaj.flightcrew.repository.AircraftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AircraftServiceTest {

    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private AircraftMapper aircraftMapper;

    @InjectMocks
    private AircraftService aircraftService;

    private Aircraft existingAircraft;

    @BeforeEach
    void setUp() {
        existingAircraft = new Aircraft();
        existingAircraft.setId(1L);
        existingAircraft.setTailNumber("N123AB");
        existingAircraft.setModel("Boeing 737-800");
        existingAircraft.setCapacity(180);
        existingAircraft.setStatus(AircraftStatus.ACTIVE);
    }

    @Test
    void createAircraft_savesAndReturnsResponse_whenTailNumberIsUnique() {
        AircraftCreateRequest request = new AircraftCreateRequest();
        request.setTailNumber("N999ZZ");

        Aircraft mappedEntity = new Aircraft();
        Aircraft savedEntity = new Aircraft();
        savedEntity.setId(2L);
        AircraftResponse expectedResponse = AircraftResponse.builder().id(2L).build();

        when(aircraftRepository.existsByTailNumber("N999ZZ")).thenReturn(false);
        when(aircraftMapper.toEntity(request)).thenReturn(mappedEntity);
        when(aircraftRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(aircraftMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        AircraftResponse result = aircraftService.createAircraft(request);

        assertThat(result).isEqualTo(expectedResponse);
        verify(aircraftRepository).save(mappedEntity);
    }

    @Test
    void createAircraft_throwsDuplicateResourceException_whenTailNumberAlreadyExists() {
        AircraftCreateRequest request = new AircraftCreateRequest();
        request.setTailNumber("N123AB");

        when(aircraftRepository.existsByTailNumber("N123AB")).thenReturn(true);

        assertThatThrownBy(() -> aircraftService.createAircraft(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("N123AB");

        verify(aircraftRepository, never()).save(any());
    }

    @Test
    void getAircraftById_returnsResponse_whenExists() {
        AircraftResponse expectedResponse = AircraftResponse.builder().id(1L).build();
        when(aircraftRepository.findById(1L)).thenReturn(Optional.of(existingAircraft));
        when(aircraftMapper.toResponse(existingAircraft)).thenReturn(expectedResponse);

        AircraftResponse result = aircraftService.getAircraftById(1L);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getAircraftById_throwsResourceNotFoundException_whenNotExists() {
        when(aircraftRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aircraftService.getAircraftById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAllAircraft_returnsMappedList() {
        Aircraft second = new Aircraft();
        second.setId(2L);
        AircraftResponse response1 = AircraftResponse.builder().id(1L).build();
        AircraftResponse response2 = AircraftResponse.builder().id(2L).build();

        when(aircraftRepository.findAll()).thenReturn(List.of(existingAircraft, second));
        when(aircraftMapper.toResponse(existingAircraft)).thenReturn(response1);
        when(aircraftMapper.toResponse(second)).thenReturn(response2);

        List<AircraftResponse> result = aircraftService.getAllAircraft();

        assertThat(result).containsExactly(response1, response2);
    }

    @Test
    void getAvailableAircraft_returnsMappedList() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 10, 11, 0);
        AircraftResponse response = AircraftResponse.builder().id(1L).build();

        when(aircraftRepository.findAvailableAircraft(start, end)).thenReturn(List.of(existingAircraft));
        when(aircraftMapper.toResponse(existingAircraft)).thenReturn(response);

        List<AircraftResponse> result = aircraftService.getAvailableAircraft(start, end);

        assertThat(result).containsExactly(response);
    }

    @Test
    void updateAircraft_updatesAndReturnsResponse_whenTailNumberUnchanged() {
        AircraftUpdateRequest request = new AircraftUpdateRequest();
        request.setTailNumber(existingAircraft.getTailNumber());

        AircraftResponse expectedResponse = AircraftResponse.builder().id(1L).build();

        when(aircraftRepository.findById(1L)).thenReturn(Optional.of(existingAircraft));
        when(aircraftRepository.save(existingAircraft)).thenReturn(existingAircraft);
        when(aircraftMapper.toResponse(existingAircraft)).thenReturn(expectedResponse);

        AircraftResponse result = aircraftService.updateAircraft(1L, request);

        assertThat(result).isEqualTo(expectedResponse);
        verify(aircraftMapper).updateEntity(existingAircraft, request);
        verify(aircraftRepository, never()).existsByTailNumber(any());
    }

    @Test
    void updateAircraft_throwsDuplicateResourceException_whenNewTailNumberBelongsToAnotherAircraft() {
        AircraftUpdateRequest request = new AircraftUpdateRequest();
        request.setTailNumber("N999ZZ");

        when(aircraftRepository.findById(1L)).thenReturn(Optional.of(existingAircraft));
        when(aircraftRepository.existsByTailNumber("N999ZZ")).thenReturn(true);

        assertThatThrownBy(() -> aircraftService.updateAircraft(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("N999ZZ");

        verify(aircraftRepository, never()).save(any());
    }

    @Test
    void updateAircraft_throwsResourceNotFoundException_whenNotExists() {
        AircraftUpdateRequest request = new AircraftUpdateRequest();
        when(aircraftRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aircraftService.updateAircraft(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(aircraftRepository, never()).save(any());
    }

    @Test
    void deleteAircraft_deletesAircraft_whenExists() {
        when(aircraftRepository.findById(1L)).thenReturn(Optional.of(existingAircraft));

        aircraftService.deleteAircraft(1L);

        verify(aircraftRepository).delete(existingAircraft);
    }

    @Test
    void deleteAircraft_throwsResourceNotFoundException_whenNotExists() {
        when(aircraftRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aircraftService.deleteAircraft(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(aircraftRepository, never()).delete(any());
    }
}
