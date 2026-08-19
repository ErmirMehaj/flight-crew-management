package com.mehaj.flightcrew.service;

import com.mehaj.flightcrew.dto.FlightResponse;
import com.mehaj.flightcrew.entity.Aircraft;
import com.mehaj.flightcrew.entity.AircraftStatus;
import com.mehaj.flightcrew.entity.CrewMember;
import com.mehaj.flightcrew.entity.CrewPosition;
import com.mehaj.flightcrew.entity.CrewStatus;
import com.mehaj.flightcrew.entity.Flight;
import com.mehaj.flightcrew.entity.FlightCrewAssignment;
import com.mehaj.flightcrew.entity.FlightPilotAssignment;
import com.mehaj.flightcrew.entity.FlightStatus;
import com.mehaj.flightcrew.entity.Pilot;
import com.mehaj.flightcrew.entity.PilotRank;
import com.mehaj.flightcrew.entity.PilotStatus;
import com.mehaj.flightcrew.entity.WorkHours;
import com.mehaj.flightcrew.exception.InvalidFlightStateException;
import com.mehaj.flightcrew.exception.ResourceNotFoundException;
import com.mehaj.flightcrew.exception.SchedulingConflictException;
import com.mehaj.flightcrew.mapper.FlightMapper;
import com.mehaj.flightcrew.repository.AircraftRepository;
import com.mehaj.flightcrew.repository.AvailabilityRepository;
import com.mehaj.flightcrew.repository.CrewMemberRepository;
import com.mehaj.flightcrew.repository.FlightCrewAssignmentRepository;
import com.mehaj.flightcrew.repository.FlightPilotAssignmentRepository;
import com.mehaj.flightcrew.repository.FlightRepository;
import com.mehaj.flightcrew.repository.PilotRepository;
import com.mehaj.flightcrew.repository.WorkHoursRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightAssignmentServiceTest {

    @Mock
    private FlightRepository flightRepository;
    @Mock
    private AircraftRepository aircraftRepository;
    @Mock
    private PilotRepository pilotRepository;
    @Mock
    private CrewMemberRepository crewMemberRepository;
    @Mock
    private FlightPilotAssignmentRepository flightPilotAssignmentRepository;
    @Mock
    private FlightCrewAssignmentRepository flightCrewAssignmentRepository;
    @Mock
    private AvailabilityRepository availabilityRepository;
    @Mock
    private WorkHoursRepository workHoursRepository;
    @Mock
    private FlightMapper flightMapper;

    @InjectMocks
    private FlightAssignmentService flightAssignmentService;

    private Flight existingFlight;

    @BeforeEach
    void setUp() {
        existingFlight = new Flight();
        existingFlight.setId(1L);
        existingFlight.setFlightNumber("FC100");
        existingFlight.setStatus(FlightStatus.SCHEDULED);
        existingFlight.setDepartureTime(LocalDateTime.of(2026, 4, 1, 9, 0));
        existingFlight.setArrivalTime(LocalDateTime.of(2026, 4, 1, 11, 0)); // 2-hour flight
    }

    private Aircraft activeAircraft() {
        Aircraft aircraft = new Aircraft();
        aircraft.setId(10L);
        aircraft.setTailNumber("N123AB");
        aircraft.setStatus(AircraftStatus.ACTIVE);
        return aircraft;
    }

    private Pilot activePilot() {
        Pilot pilot = new Pilot();
        pilot.setId(20L);
        pilot.setLicenseNumber("LIC-001");
        pilot.setStatus(PilotStatus.ACTIVE);
        pilot.setTotalFlightHours(10.0);
        return pilot;
    }

    private CrewMember activeCrewMember() {
        CrewMember crewMember = new CrewMember();
        crewMember.setId(30L);
        crewMember.setEmployeeId("EMP-001");
        crewMember.setStatus(CrewStatus.ACTIVE);
        return crewMember;
    }

    @Nested
    class AssignAircraftTests {

        @Test
        void succeeds_whenAircraftIsActiveAndHasNoOverlap() {
            Aircraft aircraft = activeAircraft();
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
            when(aircraftRepository.findById(10L)).thenReturn(Optional.of(aircraft));
            when(flightRepository.findOverlappingFlightsForAircraft(10L,
                    existingFlight.getDepartureTime(), existingFlight.getArrivalTime()))
                    .thenReturn(List.of());
            when(flightRepository.save(existingFlight)).thenReturn(existingFlight);
            when(flightMapper.toResponse(existingFlight)).thenReturn(FlightResponse.builder().id(1L).build());

            FlightResponse result = flightAssignmentService.assignAircraft(1L, 10L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(existingFlight.getAircraft()).isEqualTo(aircraft);
        }

        @Test
        void succeeds_whenTheOnlyOverlappingFlightFoundIsTheFlightItself() {
            Aircraft aircraft = activeAircraft();
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
            when(aircraftRepository.findById(10L)).thenReturn(Optional.of(aircraft));
            // The "conflict" the query finds is the very flight we're assigning to --
            // this must NOT be treated as a real conflict.
            when(flightRepository.findOverlappingFlightsForAircraft(10L,
                    existingFlight.getDepartureTime(), existingFlight.getArrivalTime()))
                    .thenReturn(List.of(existingFlight));
            when(flightRepository.save(existingFlight)).thenReturn(existingFlight);
            when(flightMapper.toResponse(existingFlight)).thenReturn(FlightResponse.builder().id(1L).build());

            FlightResponse result = flightAssignmentService.assignAircraft(1L, 10L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(existingFlight.getAircraft()).isEqualTo(aircraft);
        }

        @ParameterizedTest
        @EnumSource(value = FlightStatus.class, names = "SCHEDULED", mode = EnumSource.Mode.EXCLUDE)
        void throwsInvalidFlightStateException_whenFlightIsNotScheduled(FlightStatus status) {
            existingFlight.setStatus(status);
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));

            assertThatThrownBy(() -> flightAssignmentService.assignAircraft(1L, 10L))
                    .isInstanceOf(InvalidFlightStateException.class);

            verify(flightRepository, never()).save(any());
        }

        @Test
        void throwsResourceNotFoundException_whenAircraftDoesNotExist() {
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
            when(aircraftRepository.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> flightAssignmentService.assignAircraft(1L, 10L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void throwsSchedulingConflictException_whenAircraftIsNotActive() {
            Aircraft aircraft = activeAircraft();
            aircraft.setStatus(AircraftStatus.MAINTENANCE);
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
            when(aircraftRepository.findById(10L)).thenReturn(Optional.of(aircraft));

            assertThatThrownBy(() -> flightAssignmentService.assignAircraft(1L, 10L))
                    .isInstanceOf(SchedulingConflictException.class);

            verify(flightRepository, never()).save(any());
        }

        @Test
        void throwsSchedulingConflictException_whenAircraftOverlapsAnotherFlight() {
            Aircraft aircraft = activeAircraft();
            Flight otherFlight = new Flight();
            otherFlight.setId(2L);
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
            when(aircraftRepository.findById(10L)).thenReturn(Optional.of(aircraft));
            when(flightRepository.findOverlappingFlightsForAircraft(10L,
                    existingFlight.getDepartureTime(), existingFlight.getArrivalTime()))
                    .thenReturn(List.of(otherFlight));

            assertThatThrownBy(() -> flightAssignmentService.assignAircraft(1L, 10L))
                    .isInstanceOf(SchedulingConflictException.class);

            verify(flightRepository, never()).save(any());
        }
    }

    @Nested
    class AssignPilotTests {

        @Test
        void succeeds_whenPilotIsAvailableAndNotDoubleBooked() {
            Pilot pilot = activePilot();
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
            when(pilotRepository.findById(20L)).thenReturn(Optional.of(pilot));
            when(flightPilotAssignmentRepository.existsByFlightIdAndPilotId(1L, 20L)).thenReturn(false);
            when(availabilityRepository.existsOverlappingUnavailability(20L,
                    existingFlight.getDepartureTime(), existingFlight.getArrivalTime())).thenReturn(false);
            when(flightPilotAssignmentRepository.existsOverlappingAssignment(20L,
                    existingFlight.getDepartureTime(), existingFlight.getArrivalTime())).thenReturn(false);
            when(flightRepository.save(existingFlight)).thenReturn(existingFlight);
            when(flightMapper.toResponse(existingFlight)).thenReturn(FlightResponse.builder().id(1L).build());

            FlightResponse result = flightAssignmentService.assignPilot(1L, 20L, PilotRank.CAPTAIN);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(existingFlight.getPilotAssignments()).hasSize(1);
            assertThat(existingFlight.getPilotAssignments().get(0).getPilot()).isEqualTo(pilot);
            assertThat(existingFlight.getPilotAssignments().get(0).getRole()).isEqualTo(PilotRank.CAPTAIN);
        }

        @ParameterizedTest
        @EnumSource(value = FlightStatus.class, names = "SCHEDULED", mode = EnumSource.Mode.EXCLUDE)
        void throwsInvalidFlightStateException_whenFlightIsNotScheduled(FlightStatus status) {
            existingFlight.setStatus(status);
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));

            assertThatThrownBy(() -> flightAssignmentService.assignPilot(1L, 20L, PilotRank.CAPTAIN))
                    .isInstanceOf(InvalidFlightStateException.class);
        }

        @Test
        void throwsResourceNotFoundException_whenPilotDoesNotExist() {
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
            when(pilotRepository.findById(20L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> flightAssignmentService.assignPilot(1L, 20L, PilotRank.CAPTAIN))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void throwsSchedulingConflictException_whenPilotIsNotActive() {
            Pilot pilot = activePilot();
            pilot.setStatus(PilotStatus.SUSPENDED);
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
            when(pilotRepository.findById(20L)).thenReturn(Optional.of(pilot));

            assertThatThrownBy(() -> flightAssignmentService.assignPilot(1L, 20L, PilotRank.CAPTAIN))
                    .isInstanceOf(SchedulingConflictException.class);
        }

        @Test
        void throwsSchedulingConflictException_whenAlreadyAssignedToThisFlight() {
            Pilot pilot = activePilot();
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
            when(pilotRepository.findById(20L)).thenReturn(Optional.of(pilot));
            when(flightPilotAssignmentRepository.existsByFlightIdAndPilotId(1L, 20L)).thenReturn(true);

            assertThatThrownBy(() -> flightAssignmentService.assignPilot(1L, 20L, PilotRank.CAPTAIN))
                    .isInstanceOf(SchedulingConflictException.class);
        }

        @Test
        void throwsSchedulingConflictException_whenPilotIsUnavailable() {
            Pilot pilot = activePilot();
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
            when(pilotRepository.findById(20L)).thenReturn(Optional.of(pilot));
            when(flightPilotAssignmentRepository.existsByFlightIdAndPilotId(1L, 20L)).thenReturn(false);
            when(availabilityRepository.existsOverlappingUnavailability(20L,
                    existingFlight.getDepartureTime(), existingFlight.getArrivalTime())).thenReturn(true);

            assertThatThrownBy(() -> flightAssignmentService.assignPilot(1L, 20L, PilotRank.CAPTAIN))
                    .isInstanceOf(SchedulingConflictException.class);
        }

        @Test
        void throwsSchedulingConflictException_whenPilotIsDoubleBookedOnAnotherFlight() {
            Pilot pilot = activePilot();
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
            when(pilotRepository.findById(20L)).thenReturn(Optional.of(pilot));
            when(flightPilotAssignmentRepository.existsByFlightIdAndPilotId(1L, 20L)).thenReturn(false);
            when(availabilityRepository.existsOverlappingUnavailability(20L,
                    existingFlight.getDepartureTime(), existingFlight.getArrivalTime())).thenReturn(false);
            when(flightPilotAssignmentRepository.existsOverlappingAssignment(20L,
                    existingFlight.getDepartureTime(), existingFlight.getArrivalTime())).thenReturn(true);

            assertThatThrownBy(() -> flightAssignmentService.assignPilot(1L, 20L, PilotRank.CAPTAIN))
                    .isInstanceOf(SchedulingConflictException.class);
        }
    }

    @Nested
    class AssignCrewTests {

        @BeforeEach
        void setMaxWeeklyHours() {
            ReflectionTestUtils.setField(flightAssignmentService, "maxWeeklyHours", 40.0);
        }

        @Test
        void succeeds_whenCrewMemberIsUnderTheWeeklyLimit() {
            CrewMember crewMember = activeCrewMember();
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
            when(crewMemberRepository.findById(30L)).thenReturn(Optional.of(crewMember));
            when(flightCrewAssignmentRepository.existsByFlightIdAndCrewMemberId(1L, 30L)).thenReturn(false);
            when(flightCrewAssignmentRepository.existsOverlappingAssignment(30L,
                    existingFlight.getDepartureTime(), existingFlight.getArrivalTime())).thenReturn(false);
            // 2-hour flight; 36 hours already logged this week -> 38 total, under the 40 cap.
            when(workHoursRepository.sumHoursWorkedSince(eq(30L), any(LocalDate.class))).thenReturn(36.0);
            when(flightRepository.save(existingFlight)).thenReturn(existingFlight);
            when(flightMapper.toResponse(existingFlight)).thenReturn(FlightResponse.builder().id(1L).build());

            FlightResponse result = flightAssignmentService.assignCrew(1L, 30L, CrewPosition.FLIGHT_ATTENDANT);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(existingFlight.getCrewAssignments()).hasSize(1);
            assertThat(existingFlight.getCrewAssignments().get(0).getCrewMember()).isEqualTo(crewMember);
        }

        @ParameterizedTest
        @EnumSource(value = FlightStatus.class, names = "SCHEDULED", mode = EnumSource.Mode.EXCLUDE)
        void throwsInvalidFlightStateException_whenFlightIsNotScheduled(FlightStatus status) {
            existingFlight.setStatus(status);
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));

            assertThatThrownBy(() ->
                    flightAssignmentService.assignCrew(1L, 30L, CrewPosition.FLIGHT_ATTENDANT))
                    .isInstanceOf(InvalidFlightStateException.class);
        }

        @Test
        void throwsResourceNotFoundException_whenCrewMemberDoesNotExist() {
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
            when(crewMemberRepository.findById(30L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    flightAssignmentService.assignCrew(1L, 30L, CrewPosition.FLIGHT_ATTENDANT))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void throwsSchedulingConflictException_whenCrewMemberIsNotActive() {
            CrewMember crewMember = activeCrewMember();
            crewMember.setStatus(CrewStatus.ON_LEAVE);
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
            when(crewMemberRepository.findById(30L)).thenReturn(Optional.of(crewMember));

            assertThatThrownBy(() ->
                    flightAssignmentService.assignCrew(1L, 30L, CrewPosition.FLIGHT_ATTENDANT))
                    .isInstanceOf(SchedulingConflictException.class);
        }

        @Test
        void throwsSchedulingConflictException_whenAlreadyAssignedToThisFlight() {
            CrewMember crewMember = activeCrewMember();
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
            when(crewMemberRepository.findById(30L)).thenReturn(Optional.of(crewMember));
            when(flightCrewAssignmentRepository.existsByFlightIdAndCrewMemberId(1L, 30L)).thenReturn(true);

            assertThatThrownBy(() ->
                    flightAssignmentService.assignCrew(1L, 30L, CrewPosition.FLIGHT_ATTENDANT))
                    .isInstanceOf(SchedulingConflictException.class);
        }

        @Test
        void throwsSchedulingConflictException_whenDoubleBookedOnAnotherFlight() {
            CrewMember crewMember = activeCrewMember();
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
            when(crewMemberRepository.findById(30L)).thenReturn(Optional.of(crewMember));
            when(flightCrewAssignmentRepository.existsByFlightIdAndCrewMemberId(1L, 30L)).thenReturn(false);
            when(flightCrewAssignmentRepository.existsOverlappingAssignment(30L,
                    existingFlight.getDepartureTime(), existingFlight.getArrivalTime())).thenReturn(true);

            assertThatThrownBy(() ->
                    flightAssignmentService.assignCrew(1L, 30L, CrewPosition.FLIGHT_ATTENDANT))
                    .isInstanceOf(SchedulingConflictException.class);
        }

        @Test
        void throwsSchedulingConflictException_whenAssignmentWouldExceedWeeklyLimit() {
            CrewMember crewMember = activeCrewMember();
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
            when(crewMemberRepository.findById(30L)).thenReturn(Optional.of(crewMember));
            when(flightCrewAssignmentRepository.existsByFlightIdAndCrewMemberId(1L, 30L)).thenReturn(false);
            when(flightCrewAssignmentRepository.existsOverlappingAssignment(30L,
                    existingFlight.getDepartureTime(), existingFlight.getArrivalTime())).thenReturn(false);
            // 2-hour flight; 39 hours already logged -> 41 total, over the 40 cap.
            when(workHoursRepository.sumHoursWorkedSince(eq(30L), any(LocalDate.class))).thenReturn(39.0);

            assertThatThrownBy(() ->
                    flightAssignmentService.assignCrew(1L, 30L, CrewPosition.FLIGHT_ATTENDANT))
                    .isInstanceOf(SchedulingConflictException.class);

            verify(flightRepository, never()).save(any());
        }
    }

    @Nested
    class CompleteFlightTests {

        @Test
        void creditsPilotHours_createsWorkHoursForCrew_andMarksFlightCompleted() {
            Aircraft aircraft = activeAircraft();
            existingFlight.setAircraft(aircraft);

            Pilot pilot = activePilot(); // starts at 10.0 hours
            FlightPilotAssignment pilotAssignment = new FlightPilotAssignment();
            pilotAssignment.setPilot(pilot);
            existingFlight.getPilotAssignments().add(pilotAssignment);

            CrewMember crewMember = activeCrewMember();
            FlightCrewAssignment crewAssignment = new FlightCrewAssignment();
            crewAssignment.setCrewMember(crewMember);
            existingFlight.getCrewAssignments().add(crewAssignment);

            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
            when(flightRepository.save(existingFlight)).thenReturn(existingFlight);

            flightAssignmentService.completeFlight(1L);

            // Pilot hours: started at 10.0, flight is 2 hours -> 12.0
            assertThat(pilot.getTotalFlightHours()).isEqualTo(12.0);
            assertThat(existingFlight.getStatus()).isEqualTo(FlightStatus.COMPLETED);

            ArgumentCaptor<WorkHours> workHoursCaptor = ArgumentCaptor.forClass(WorkHours.class);
            verify(workHoursRepository).save(workHoursCaptor.capture());
            WorkHours saved = workHoursCaptor.getValue();
            assertThat(saved.getCrewMember()).isEqualTo(crewMember);
            assertThat(saved.getFlight()).isEqualTo(existingFlight);
            assertThat(saved.getHoursWorked()).isEqualTo(2.0);
            assertThat(saved.getWorkDate()).isEqualTo(LocalDate.of(2026, 4, 1));
        }

        @ParameterizedTest
        @EnumSource(value = FlightStatus.class, names = "SCHEDULED", mode = EnumSource.Mode.EXCLUDE)
        void throwsInvalidFlightStateException_whenFlightIsNotScheduled(FlightStatus status) {
            existingFlight.setStatus(status);
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));

            assertThatThrownBy(() -> flightAssignmentService.completeFlight(1L))
                    .isInstanceOf(InvalidFlightStateException.class);
        }

        @Test
        void throwsInvalidFlightStateException_whenNoAircraftAssigned() {
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));

            assertThatThrownBy(() -> flightAssignmentService.completeFlight(1L))
                    .isInstanceOf(InvalidFlightStateException.class);

            verify(flightRepository, never()).save(any());
        }

        @Test
        void throwsInvalidFlightStateException_whenNoPilotsAssigned() {
            existingFlight.setAircraft(activeAircraft());
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));

            assertThatThrownBy(() -> flightAssignmentService.completeFlight(1L))
                    .isInstanceOf(InvalidFlightStateException.class);

            verify(flightRepository, never()).save(any());
        }

        @Test
        void doesNotCreateAnyWorkHours_whenNoCrewAssigned() {
            existingFlight.setAircraft(activeAircraft());
            FlightPilotAssignment pilotAssignment = new FlightPilotAssignment();
            pilotAssignment.setPilot(activePilot());
            existingFlight.getPilotAssignments().add(pilotAssignment);

            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
            when(flightRepository.save(existingFlight)).thenReturn(existingFlight);

            flightAssignmentService.completeFlight(1L);

            assertThat(existingFlight.getStatus()).isEqualTo(FlightStatus.COMPLETED);
            verify(workHoursRepository, never()).save(any());
        }
    }

    @Nested
    class CancelFlightTests {

        @Test
        void marksFlightCancelled_whenScheduled() {
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));
            when(flightRepository.save(existingFlight)).thenReturn(existingFlight);

            flightAssignmentService.cancelFlight(1L);

            assertThat(existingFlight.getStatus()).isEqualTo(FlightStatus.CANCELLED);
        }

        @ParameterizedTest
        @EnumSource(value = FlightStatus.class, names = "SCHEDULED", mode = EnumSource.Mode.EXCLUDE)
        void throwsInvalidFlightStateException_whenFlightIsNotScheduled(FlightStatus status) {
            existingFlight.setStatus(status);
            when(flightRepository.findById(1L)).thenReturn(Optional.of(existingFlight));

            assertThatThrownBy(() -> flightAssignmentService.cancelFlight(1L))
                    .isInstanceOf(InvalidFlightStateException.class);

            verify(flightRepository, never()).save(any());
        }
    }
}
