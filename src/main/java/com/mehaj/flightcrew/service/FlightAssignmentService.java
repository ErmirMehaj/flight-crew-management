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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Owns the Flight lifecycle actions that reach across multiple entities:
 * assigning aircraft/pilots/crew, and completing/cancelling a flight.
 * Kept separate from FlightService (plain CRUD) so that service stays
 * small, and so this one class is the single place to look for every
 * cross-entity scheduling rule in the system.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightAssignmentService {

    private final FlightRepository flightRepository;
    private final AircraftRepository aircraftRepository;
    private final PilotRepository pilotRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final FlightPilotAssignmentRepository flightPilotAssignmentRepository;
    private final FlightCrewAssignmentRepository flightCrewAssignmentRepository;
    private final AvailabilityRepository availabilityRepository;
    private final WorkHoursRepository workHoursRepository;
    private final FlightMapper flightMapper;

    @Value("${crew.max-weekly-hours}")
    private double maxWeeklyHours;

    @Transactional
    public FlightResponse assignAircraft(Long flightId, Long aircraftId) {
        Flight flight = findFlightOrThrow(flightId);
        requireScheduled(flight);

        Aircraft aircraft = aircraftRepository.findById(aircraftId)
                .orElseThrow(() -> new ResourceNotFoundException("Aircraft not found with id " + aircraftId));

        if (aircraft.getStatus() != AircraftStatus.ACTIVE) {
            throw new SchedulingConflictException(
                    "Aircraft " + aircraft.getTailNumber() + " is not ACTIVE (status: " + aircraft.getStatus() + ")");
        }

        List<Flight> overlapping = flightRepository.findOverlappingFlightsForAircraft(
                aircraftId, flight.getDepartureTime(), flight.getArrivalTime());
        boolean conflict = overlapping.stream().anyMatch(f -> !f.getId().equals(flightId));
        if (conflict) {
            throw new SchedulingConflictException(
                    "Aircraft " + aircraft.getTailNumber() + " is already assigned to an overlapping flight");
        }

        flight.setAircraft(aircraft);
        Flight saved = flightRepository.save(flight);
        log.info("Assigned aircraft id={} to flight id={}", aircraftId, flightId);
        return flightMapper.toResponse(saved);
    }

    @Transactional
    public FlightResponse assignPilot(Long flightId, Long pilotId, PilotRank role) {
        Flight flight = findFlightOrThrow(flightId);
        requireScheduled(flight);

        Pilot pilot = pilotRepository.findById(pilotId)
                .orElseThrow(() -> new ResourceNotFoundException("Pilot not found with id " + pilotId));

        if (pilot.getStatus() != PilotStatus.ACTIVE) {
            throw new SchedulingConflictException(
                    "Pilot " + pilot.getLicenseNumber() + " is not ACTIVE (status: " + pilot.getStatus() + ")");
        }

        if (flightPilotAssignmentRepository.existsByFlightIdAndPilotId(flightId, pilotId)) {
            throw new SchedulingConflictException(
                    "Pilot " + pilot.getLicenseNumber() + " is already assigned to this flight");
        }

        if (availabilityRepository.existsOverlappingUnavailability(
                pilotId, flight.getDepartureTime(), flight.getArrivalTime())) {
            throw new SchedulingConflictException(
                    "Pilot " + pilot.getLicenseNumber() + " is unavailable during this flight's schedule");
        }

        if (flightPilotAssignmentRepository.existsOverlappingAssignment(
                pilotId, flight.getDepartureTime(), flight.getArrivalTime())) {
            throw new SchedulingConflictException(
                    "Pilot " + pilot.getLicenseNumber() + " is already assigned to another overlapping flight");
        }

        FlightPilotAssignment assignment = FlightPilotAssignment.builder()
                .flight(flight)
                .pilot(pilot)
                .role(role)
                .assignedAt(LocalDateTime.now())
                .build();
        flight.getPilotAssignments().add(assignment);
        Flight saved = flightRepository.save(flight);

        log.info("Assigned pilot id={} to flight id={} role={}", pilotId, flightId, role);
        return flightMapper.toResponse(saved);
    }

    @Transactional
    public FlightResponse assignCrew(Long flightId, Long crewMemberId, CrewPosition role) {
        Flight flight = findFlightOrThrow(flightId);
        requireScheduled(flight);

        CrewMember crewMember = crewMemberRepository.findById(crewMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("Crew member not found with id " + crewMemberId));

        if (crewMember.getStatus() != CrewStatus.ACTIVE) {
            throw new SchedulingConflictException(
                    "Crew member " + crewMember.getEmployeeId() + " is not ACTIVE (status: " + crewMember.getStatus() + ")");
        }

        if (flightCrewAssignmentRepository.existsByFlightIdAndCrewMemberId(flightId, crewMemberId)) {
            throw new SchedulingConflictException(
                    "Crew member " + crewMember.getEmployeeId() + " is already assigned to this flight");
        }

        if (flightCrewAssignmentRepository.existsOverlappingAssignment(
                crewMemberId, flight.getDepartureTime(), flight.getArrivalTime())) {
            throw new SchedulingConflictException(
                    "Crew member " + crewMember.getEmployeeId() + " is already assigned to another overlapping flight");
        }

        double flightHours = calculateFlightHours(flight);
        LocalDate windowStart = flight.getDepartureTime().toLocalDate().minusDays(6);
        double hoursAlreadyLogged = workHoursRepository.sumHoursWorkedSince(crewMemberId, windowStart);
        if (hoursAlreadyLogged + flightHours > maxWeeklyHours) {
            throw new SchedulingConflictException(
                    "Crew member " + crewMember.getEmployeeId() + " would exceed the " + maxWeeklyHours
                            + "-hour weekly limit (" + hoursAlreadyLogged + " logged + " + flightHours + " for this flight)");
        }

        FlightCrewAssignment assignment = FlightCrewAssignment.builder()
                .flight(flight)
                .crewMember(crewMember)
                .role(role)
                .assignedAt(LocalDateTime.now())
                .build();
        flight.getCrewAssignments().add(assignment);
        Flight saved = flightRepository.save(flight);

        log.info("Assigned crew member id={} to flight id={} role={}", crewMemberId, flightId, role);
        return flightMapper.toResponse(saved);
    }

    @Transactional
    public FlightResponse completeFlight(Long flightId) {
        Flight flight = findFlightOrThrow(flightId);
        requireScheduled(flight);

        if (flight.getAircraft() == null) {
            throw new InvalidFlightStateException(
                    "Cannot complete flight " + flightId + " because it has no aircraft assigned");
        }
        if (flight.getPilotAssignments().isEmpty()) {
            throw new InvalidFlightStateException(
                    "Cannot complete flight " + flightId + " because it has no pilots assigned");
        }

        double flightHours = calculateFlightHours(flight);
        LocalDate flightDate = flight.getDepartureTime().toLocalDate();

        for (FlightPilotAssignment assignment : flight.getPilotAssignments()) {
            Pilot pilot = assignment.getPilot();
            pilot.setTotalFlightHours(pilot.getTotalFlightHours() + flightHours);
        }

        for (FlightCrewAssignment assignment : flight.getCrewAssignments()) {
            WorkHours workHours = WorkHours.builder()
                    .crewMember(assignment.getCrewMember())
                    .flight(flight)
                    .hoursWorked(flightHours)
                    .workDate(flightDate)
                    .build();
            workHoursRepository.save(workHours);
        }

        flight.setStatus(FlightStatus.COMPLETED);
        Flight saved = flightRepository.save(flight);

        log.info("Completed flight id={}: {} pilot(s) credited {} hours, {} work-hours record(s) created",
                flightId, flight.getPilotAssignments().size(), flightHours, flight.getCrewAssignments().size());
        return flightMapper.toResponse(saved);
    }

    private double calculateFlightHours(Flight flight) {
        return Duration.between(flight.getDepartureTime(), flight.getArrivalTime()).toMinutes() / 60.0;
    }

    private Flight findFlightOrThrow(Long flightId) {
        return flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id " + flightId));
    }

    private void requireScheduled(Flight flight) {
        if (flight.getStatus() != FlightStatus.SCHEDULED) {
            throw new InvalidFlightStateException(
                    "Flight " + flight.getId() + " is not SCHEDULED (status: " + flight.getStatus() + ")");
        }
    }
}
