package com.mehaj.flightcrew.service;

import com.mehaj.flightcrew.dto.FlightResponse;
import com.mehaj.flightcrew.entity.Aircraft;
import com.mehaj.flightcrew.entity.AircraftStatus;
import com.mehaj.flightcrew.entity.Flight;
import com.mehaj.flightcrew.entity.FlightStatus;
import com.mehaj.flightcrew.exception.InvalidFlightStateException;
import com.mehaj.flightcrew.exception.ResourceNotFoundException;
import com.mehaj.flightcrew.exception.SchedulingConflictException;
import com.mehaj.flightcrew.mapper.FlightMapper;
import com.mehaj.flightcrew.repository.AircraftRepository;
import com.mehaj.flightcrew.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final FlightMapper flightMapper;

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
