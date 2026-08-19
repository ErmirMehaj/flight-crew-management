package com.mehaj.flightcrew.service;

import com.mehaj.flightcrew.dto.PilotCreateRequest;
import com.mehaj.flightcrew.dto.PilotResponse;
import com.mehaj.flightcrew.dto.PilotUpdateRequest;
import com.mehaj.flightcrew.entity.Pilot;
import com.mehaj.flightcrew.exception.DuplicateResourceException;
import com.mehaj.flightcrew.exception.ResourceNotFoundException;
import com.mehaj.flightcrew.mapper.PilotMapper;
import com.mehaj.flightcrew.repository.PilotRepository;
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
public class PilotService {

    private final PilotRepository pilotRepository;
    private final PilotMapper pilotMapper;

    @Transactional
    public PilotResponse createPilot(PilotCreateRequest request) {
        if (pilotRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new DuplicateResourceException(
                    "Pilot with license number '" + request.getLicenseNumber() + "' already exists");
        }
        if (pilotRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Pilot with email '" + request.getEmail() + "' already exists");
        }

        Pilot pilot = pilotMapper.toEntity(request);
        Pilot saved = pilotRepository.save(pilot);
        log.info("Created pilot id={} licenseNumber={}", saved.getId(), saved.getLicenseNumber());
        return pilotMapper.toResponse(saved);
    }

    public PilotResponse getPilotById(Long id) {
        return pilotMapper.toResponse(findPilotOrThrow(id));
    }

    public List<PilotResponse> getAllPilots() {
        return pilotRepository.findAll().stream()
                .map(pilotMapper::toResponse)
                .toList();
    }

    public List<PilotResponse> getAvailablePilots(LocalDateTime start, LocalDateTime end) {
        return pilotRepository.findAvailablePilots(start, end).stream()
                .map(pilotMapper::toResponse)
                .toList();
    }

    @Transactional
    public PilotResponse updatePilot(Long id, PilotUpdateRequest request) {
        Pilot pilot = findPilotOrThrow(id);

        boolean licenseChanged = !pilot.getLicenseNumber().equals(request.getLicenseNumber());
        if (licenseChanged && pilotRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new DuplicateResourceException(
                    "Pilot with license number '" + request.getLicenseNumber() + "' already exists");
        }

        boolean emailChanged = !pilot.getEmail().equals(request.getEmail());
        if (emailChanged && pilotRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Pilot with email '" + request.getEmail() + "' already exists");
        }

        pilotMapper.updateEntity(pilot, request);
        Pilot saved = pilotRepository.save(pilot);
        log.info("Updated pilot id={}", saved.getId());
        return pilotMapper.toResponse(saved);
    }

    @Transactional
    public void deletePilot(Long id) {
        Pilot pilot = findPilotOrThrow(id);
        pilotRepository.delete(pilot);
        log.info("Deleted pilot id={}", id);
    }

    private Pilot findPilotOrThrow(Long id) {
        return pilotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pilot not found with id " + id));
    }
}
