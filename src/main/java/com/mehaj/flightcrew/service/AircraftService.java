package com.mehaj.flightcrew.service;

import com.mehaj.flightcrew.dto.AircraftCreateRequest;
import com.mehaj.flightcrew.dto.AircraftResponse;
import com.mehaj.flightcrew.dto.AircraftUpdateRequest;
import com.mehaj.flightcrew.entity.Aircraft;
import com.mehaj.flightcrew.exception.DuplicateResourceException;
import com.mehaj.flightcrew.exception.ResourceNotFoundException;
import com.mehaj.flightcrew.mapper.AircraftMapper;
import com.mehaj.flightcrew.repository.AircraftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AircraftService {

    private final AircraftRepository aircraftRepository;
    private final AircraftMapper aircraftMapper;

    @Transactional
    public AircraftResponse createAircraft(AircraftCreateRequest request) {
        if (aircraftRepository.existsByTailNumber(request.getTailNumber())) {
            throw new DuplicateResourceException(
                    "Aircraft with tail number '" + request.getTailNumber() + "' already exists");
        }

        Aircraft aircraft = aircraftMapper.toEntity(request);
        Aircraft saved = aircraftRepository.save(aircraft);
        log.info("Created aircraft id={} tailNumber={}", saved.getId(), saved.getTailNumber());
        return aircraftMapper.toResponse(saved);
    }

    public AircraftResponse getAircraftById(Long id) {
        return aircraftMapper.toResponse(findAircraftOrThrow(id));
    }

    public List<AircraftResponse> getAllAircraft() {
        return aircraftRepository.findAll().stream()
                .map(aircraftMapper::toResponse)
                .toList();
    }

    public List<AircraftResponse> getAvailableAircraft(LocalDateTime start, LocalDateTime end) {
        return aircraftRepository.findAvailableAircraft(start, end).stream()
                .map(aircraftMapper::toResponse)
                .toList();
    }

    @Transactional
    public AircraftResponse updateAircraft(Long id, AircraftUpdateRequest request) {
        Aircraft aircraft = findAircraftOrThrow(id);

        boolean tailNumberChanged = !aircraft.getTailNumber().equals(request.getTailNumber());
        if (tailNumberChanged && aircraftRepository.existsByTailNumber(request.getTailNumber())) {
            throw new DuplicateResourceException(
                    "Aircraft with tail number '" + request.getTailNumber() + "' already exists");
        }

        aircraftMapper.updateEntity(aircraft, request);
        Aircraft saved = aircraftRepository.save(aircraft);
        log.info("Updated aircraft id={}", saved.getId());
        return aircraftMapper.toResponse(saved);
    }

    @Transactional
    public void deleteAircraft(Long id) {
        Aircraft aircraft = findAircraftOrThrow(id);
        aircraftRepository.delete(aircraft);
        log.info("Deleted aircraft id={}", id);
    }

    private Aircraft findAircraftOrThrow(Long id) {
        return aircraftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aircraft not found with id " + id));
    }
}
