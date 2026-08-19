package com.mehaj.flightcrew.service;

import com.mehaj.flightcrew.dto.PilotCreateRequest;
import com.mehaj.flightcrew.dto.PilotResponse;
import com.mehaj.flightcrew.dto.PilotStatisticsResponse;
import com.mehaj.flightcrew.dto.PilotUpdateRequest;
import com.mehaj.flightcrew.entity.Flight;
import com.mehaj.flightcrew.entity.FlightPilotAssignment;
import com.mehaj.flightcrew.entity.FlightStatus;
import com.mehaj.flightcrew.entity.Pilot;
import com.mehaj.flightcrew.entity.PilotRank;
import com.mehaj.flightcrew.entity.PilotStatus;
import com.mehaj.flightcrew.exception.DuplicateResourceException;
import com.mehaj.flightcrew.exception.ResourceNotFoundException;
import com.mehaj.flightcrew.mapper.PilotMapper;
import com.mehaj.flightcrew.repository.FlightPilotAssignmentRepository;
import com.mehaj.flightcrew.repository.PilotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PilotServiceTest {

    @Mock
    private PilotRepository pilotRepository;

    @Mock
    private FlightPilotAssignmentRepository flightPilotAssignmentRepository;

    @Mock
    private PilotMapper pilotMapper;

    @InjectMocks
    private PilotService pilotService;

    private Pilot existingPilot;

    @BeforeEach
    void setUp() {
        existingPilot = new Pilot();
        existingPilot.setId(1L);
        existingPilot.setFirstName("Jane");
        existingPilot.setLastName("Doe");
        existingPilot.setLicenseNumber("LIC-001");
        existingPilot.setEmail("jane.doe@example.com");
        existingPilot.setRank(PilotRank.CAPTAIN);
        existingPilot.setStatus(PilotStatus.ACTIVE);
        existingPilot.setTotalFlightHours(0.0);
    }

    @Test
    void createPilot_savesAndReturnsResponse_whenLicenseNumberAndEmailAreUnique() {
        PilotCreateRequest request = new PilotCreateRequest();
        request.setLicenseNumber("LIC-002");
        request.setEmail("new.pilot@example.com");

        Pilot mappedEntity = new Pilot();
        Pilot savedEntity = new Pilot();
        savedEntity.setId(2L);
        PilotResponse expectedResponse = PilotResponse.builder().id(2L).build();

        when(pilotRepository.existsByLicenseNumber("LIC-002")).thenReturn(false);
        when(pilotRepository.existsByEmail("new.pilot@example.com")).thenReturn(false);
        when(pilotMapper.toEntity(request)).thenReturn(mappedEntity);
        when(pilotRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(pilotMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        PilotResponse result = pilotService.createPilot(request);

        assertThat(result).isEqualTo(expectedResponse);
        verify(pilotRepository).save(mappedEntity);
    }

    @Test
    void createPilot_throwsDuplicateResourceException_whenLicenseNumberAlreadyExists() {
        PilotCreateRequest request = new PilotCreateRequest();
        request.setLicenseNumber("LIC-001");
        request.setEmail("new.pilot@example.com");

        when(pilotRepository.existsByLicenseNumber("LIC-001")).thenReturn(true);

        assertThatThrownBy(() -> pilotService.createPilot(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("LIC-001");

        verify(pilotRepository, never()).save(any());
    }

    @Test
    void createPilot_throwsDuplicateResourceException_whenEmailAlreadyExists() {
        PilotCreateRequest request = new PilotCreateRequest();
        request.setLicenseNumber("LIC-002");
        request.setEmail("jane.doe@example.com");

        when(pilotRepository.existsByLicenseNumber("LIC-002")).thenReturn(false);
        when(pilotRepository.existsByEmail("jane.doe@example.com")).thenReturn(true);

        assertThatThrownBy(() -> pilotService.createPilot(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("jane.doe@example.com");

        verify(pilotRepository, never()).save(any());
    }

    @Test
    void getPilotById_returnsResponse_whenPilotExists() {
        PilotResponse expectedResponse = PilotResponse.builder().id(1L).build();
        when(pilotRepository.findById(1L)).thenReturn(Optional.of(existingPilot));
        when(pilotMapper.toResponse(existingPilot)).thenReturn(expectedResponse);

        PilotResponse result = pilotService.getPilotById(1L);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getPilotById_throwsResourceNotFoundException_whenPilotDoesNotExist() {
        when(pilotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pilotService.getPilotById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAllPilots_returnsMappedList() {
        Pilot secondPilot = new Pilot();
        secondPilot.setId(2L);
        PilotResponse response1 = PilotResponse.builder().id(1L).build();
        PilotResponse response2 = PilotResponse.builder().id(2L).build();

        when(pilotRepository.findAll()).thenReturn(List.of(existingPilot, secondPilot));
        when(pilotMapper.toResponse(existingPilot)).thenReturn(response1);
        when(pilotMapper.toResponse(secondPilot)).thenReturn(response2);

        List<PilotResponse> result = pilotService.getAllPilots();

        assertThat(result).containsExactly(response1, response2);
    }

    @Test
    void updatePilot_updatesAndReturnsResponse_whenLicenseNumberAndEmailUnchanged() {
        PilotUpdateRequest request = new PilotUpdateRequest();
        request.setLicenseNumber(existingPilot.getLicenseNumber());
        request.setEmail(existingPilot.getEmail());

        PilotResponse expectedResponse = PilotResponse.builder().id(1L).build();

        when(pilotRepository.findById(1L)).thenReturn(Optional.of(existingPilot));
        when(pilotRepository.save(existingPilot)).thenReturn(existingPilot);
        when(pilotMapper.toResponse(existingPilot)).thenReturn(expectedResponse);

        PilotResponse result = pilotService.updatePilot(1L, request);

        assertThat(result).isEqualTo(expectedResponse);
        verify(pilotMapper).updateEntity(existingPilot, request);
        verify(pilotRepository, never()).existsByLicenseNumber(any());
        verify(pilotRepository, never()).existsByEmail(any());
    }

    @Test
    void updatePilot_throwsDuplicateResourceException_whenNewLicenseNumberBelongsToAnotherPilot() {
        PilotUpdateRequest request = new PilotUpdateRequest();
        request.setLicenseNumber("LIC-999");
        request.setEmail(existingPilot.getEmail());

        when(pilotRepository.findById(1L)).thenReturn(Optional.of(existingPilot));
        when(pilotRepository.existsByLicenseNumber("LIC-999")).thenReturn(true);

        assertThatThrownBy(() -> pilotService.updatePilot(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("LIC-999");

        verify(pilotRepository, never()).save(any());
    }

    @Test
    void updatePilot_throwsResourceNotFoundException_whenPilotDoesNotExist() {
        PilotUpdateRequest request = new PilotUpdateRequest();
        when(pilotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pilotService.updatePilot(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(pilotRepository, never()).save(any());
    }

    @Test
    void deletePilot_deletesPilot_whenExists() {
        when(pilotRepository.findById(1L)).thenReturn(Optional.of(existingPilot));

        pilotService.deletePilot(1L);

        verify(pilotRepository).delete(existingPilot);
    }

    @Test
    void deletePilot_throwsResourceNotFoundException_whenPilotDoesNotExist() {
        when(pilotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pilotService.deletePilot(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(pilotRepository, never()).delete(any());
    }

    @Test
    void getPilotStatistics_countsCompletedAndUpcomingFlightsSeparately() {
        Flight completedFlight = new Flight();
        completedFlight.setStatus(FlightStatus.COMPLETED);
        Flight scheduledFlight = new Flight();
        scheduledFlight.setStatus(FlightStatus.SCHEDULED);

        FlightPilotAssignment completedAssignment = new FlightPilotAssignment();
        completedAssignment.setFlight(completedFlight);
        FlightPilotAssignment scheduledAssignment = new FlightPilotAssignment();
        scheduledAssignment.setFlight(scheduledFlight);

        existingPilot.setTotalFlightHours(12.5);

        when(pilotRepository.findById(1L)).thenReturn(Optional.of(existingPilot));
        when(flightPilotAssignmentRepository.findByPilotId(1L))
                .thenReturn(List.of(completedAssignment, scheduledAssignment));

        PilotStatisticsResponse result = pilotService.getPilotStatistics(1L);

        assertThat(result.getTotalFlightsCompleted()).isEqualTo(1);
        assertThat(result.getUpcomingFlightsScheduled()).isEqualTo(1);
        assertThat(result.getTotalFlightHours()).isEqualTo(12.5);
    }
}
