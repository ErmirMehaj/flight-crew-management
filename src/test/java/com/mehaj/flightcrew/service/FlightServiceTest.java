package com.mehaj.flightcrew.service;

import com.mehaj.flightcrew.dto.FlightCreateRequest;
import com.mehaj.flightcrew.dto.FlightResponse;
import com.mehaj.flightcrew.dto.FlightUpdateRequest;
import com.mehaj.flightcrew.entity.Flight;
import com.mehaj.flightcrew.entity.FlightStatus;
import com.mehaj.flightcrew.exception.DuplicateResourceException;
import com.mehaj.flightcrew.exception.InvalidFlightStateException;
import com.mehaj.flightcrew.exception.ResourceNotFoundException;
import com.mehaj.flightcrew.mapper.FlightMapper;
import com.mehaj.flightcrew.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private FlightMapper flightMapper;

    @InjectMocks
    private FlightService flightService;

    private Flight existingFlight;

    @BeforeEach
    void setUp() {
        existingFlight = new Flight();
        existingFlight.setId(1L);
        existingFlight.setFlightNumber("FC100");
        existingFlight.setDepartureAirport("JFK");
        existingFlight.setArrivalAirport("LAX");
        existingFlight.setDepartureTime(LocalDateTime.of(2026, 4, 1, 9, 0));
        existingFlight.setArrivalTime(LocalDateTime.of(2026, 4, 1, 12, 0));
        existingFlight.setStatus(FlightStatus.SCHEDULED);
    }

    @Test
    void createFlight_savesAndReturnsResponse_whenFlightNumberIsUnique() {
        FlightCreateRequest request = new FlightCreateRequest();
        request.setFlightNumber("FC200");

        Flight mappedEntity = new Flight();
        Flight savedEntity = new Flight();
        savedEntity.setId(2L);
        FlightResponse expectedResponse = FlightResponse.builder().id(2L).build();

        when(flightRepository.existsByFlightNumber("FC200")).thenReturn(false);
        when(flightMapper.toEntity(request)).thenReturn(mappedEntity);
        when(flightRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(flightMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        FlightResponse result = flightService.createFlight(request);

        assertThat(result).isEqualTo(expectedResponse);
        verify(flightRepository).save(mappedEntity);
    }

    @Test
    void createFlight_throwsDuplicateResourceException_whenFlightNumberAlreadyExists() {
        FlightCreateRequest request = new FlightCreateRequest();
        request.setFlightNumber("FC100");

        when(flightRepository.existsByFlightNumber("FC100")).thenReturn(true);

        assertThatThrownBy(() -> flightService.createFlight(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("FC100");

        verify(flightRepository, never()).save(any());
    }

    @Test
    void getFlightById_returnsResponse_whenExists() {
        FlightResponse expectedResponse = FlightResponse.builder().id(1L).build();
        when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
        when(flightMapper.toResponse(existingFlight)).thenReturn(expectedResponse);

        FlightResponse result = flightService.getFlightById(1L);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getFlightById_throwsResourceNotFoundException_whenNotExists() {
        when(flightRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.getFlightById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAllFlights_returnsMappedList() {
        Flight second = new Flight();
        second.setId(2L);
        FlightResponse response1 = FlightResponse.builder().id(1L).build();
        FlightResponse response2 = FlightResponse.builder().id(2L).build();

        when(flightRepository.findAll()).thenReturn(List.of(existingFlight, second));
        when(flightMapper.toResponse(existingFlight)).thenReturn(response1);
        when(flightMapper.toResponse(second)).thenReturn(response2);

        List<FlightResponse> result = flightService.getAllFlights();

        assertThat(result).containsExactly(response1, response2);
    }

    @Test
    void updateFlight_updatesAndReturnsResponse_whenScheduledAndFlightNumberUnchanged() {
        FlightUpdateRequest request = new FlightUpdateRequest();
        request.setFlightNumber(existingFlight.getFlightNumber());

        FlightResponse expectedResponse = FlightResponse.builder().id(1L).build();

        when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
        when(flightRepository.save(existingFlight)).thenReturn(existingFlight);
        when(flightMapper.toResponse(existingFlight)).thenReturn(expectedResponse);

        FlightResponse result = flightService.updateFlight(1L, request);

        assertThat(result).isEqualTo(expectedResponse);
        verify(flightMapper).updateEntity(existingFlight, request);
        verify(flightRepository, never()).existsByFlightNumber(any());
    }

    @ParameterizedTest
    @EnumSource(value = FlightStatus.class, names = "SCHEDULED", mode = EnumSource.Mode.EXCLUDE)
    void updateFlight_throwsInvalidFlightStateException_whenFlightIsNotScheduled(FlightStatus status) {
        existingFlight.setStatus(status);
        FlightUpdateRequest request = new FlightUpdateRequest();

        when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));

        assertThatThrownBy(() -> flightService.updateFlight(1L, request))
                .isInstanceOf(InvalidFlightStateException.class);

        verify(flightRepository, never()).save(any());
    }

    @Test
    void updateFlight_throwsDuplicateResourceException_whenNewFlightNumberBelongsToAnotherFlight() {
        FlightUpdateRequest request = new FlightUpdateRequest();
        request.setFlightNumber("FC999");

        when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
        when(flightRepository.existsByFlightNumber("FC999")).thenReturn(true);

        assertThatThrownBy(() -> flightService.updateFlight(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("FC999");

        verify(flightRepository, never()).save(any());
    }

    @Test
    void updateFlight_throwsResourceNotFoundException_whenNotExists() {
        FlightUpdateRequest request = new FlightUpdateRequest();
        when(flightRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.updateFlight(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(flightRepository, never()).save(any());
    }

    @Test
    void deleteFlight_deletesFlight_whenExists() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));

        flightService.deleteFlight(1L);

        verify(flightRepository).delete(existingFlight);
    }

    @Test
    void deleteFlight_throwsResourceNotFoundException_whenNotExists() {
        when(flightRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.deleteFlight(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(flightRepository, never()).delete(any());
    }
}
