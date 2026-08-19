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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightService {

    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;

    @Transactional
    public FlightResponse createFlight(FlightCreateRequest request) {
        if (flightRepository.existsByFlightNumber(request.getFlightNumber())) {
            throw new DuplicateResourceException(
                    "Flight with number '" + request.getFlightNumber() + "' already exists");
        }

        Flight flight = flightMapper.toEntity(request);
        Flight saved = flightRepository.save(flight);
        log.info("Created flight id={} flightNumber={}", saved.getId(), saved.getFlightNumber());
        return flightMapper.toResponse(saved);
    }

    public FlightResponse getFlightById(Long id) {
        return flightMapper.toResponse(findFlightOrThrow(id));
    }

    public List<FlightResponse> getAllFlights() {
        return flightRepository.findAll().stream()
                .map(flightMapper::toResponse)
                .toList();
    }

    @Transactional
    public FlightResponse updateFlight(Long id, FlightUpdateRequest request) {
        Flight flight = findFlightOrThrow(id);

        if (flight.getStatus() != FlightStatus.SCHEDULED) {
            throw new InvalidFlightStateException(
                    "Cannot modify flight " + id + " because it is " + flight.getStatus());
        }

        boolean flightNumberChanged = !flight.getFlightNumber().equals(request.getFlightNumber());
        if (flightNumberChanged && flightRepository.existsByFlightNumber(request.getFlightNumber())) {
            throw new DuplicateResourceException(
                    "Flight with number '" + request.getFlightNumber() + "' already exists");
        }

        flightMapper.updateEntity(flight, request);
        Flight saved = flightRepository.save(flight);
        log.info("Updated flight id={}", saved.getId());
        return flightMapper.toResponse(saved);
    }

    @Transactional
    public void deleteFlight(Long id) {
        Flight flight = findFlightOrThrow(id);
        flightRepository.delete(flight);
        log.info("Deleted flight id={}", id);
    }

    private Flight findFlightOrThrow(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id " + id));
    }
}
