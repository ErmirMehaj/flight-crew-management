package com.mehaj.flightcrew.controller;

import com.mehaj.flightcrew.dto.AvailabilityQuery;
import com.mehaj.flightcrew.dto.PilotCreateRequest;
import com.mehaj.flightcrew.dto.PilotResponse;
import com.mehaj.flightcrew.dto.PilotUpdateRequest;
import com.mehaj.flightcrew.service.PilotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pilots")
@RequiredArgsConstructor
public class PilotController {

    private final PilotService pilotService;

    @PostMapping
    public ResponseEntity<PilotResponse> createPilot(@Valid @RequestBody PilotCreateRequest request) {
        PilotResponse created = pilotService.createPilot(request);
        return ResponseEntity.created(URI.create("/api/pilots/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<PilotResponse>> getAllPilots() {
        return ResponseEntity.ok(pilotService.getAllPilots());
    }

    @GetMapping("/available")
    public ResponseEntity<List<PilotResponse>> getAvailablePilots(@Valid AvailabilityQuery query) {
        return ResponseEntity.ok(pilotService.getAvailablePilots(query.getDepartureTime(), query.getArrivalTime()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PilotResponse> getPilotById(@PathVariable Long id) {
        return ResponseEntity.ok(pilotService.getPilotById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PilotResponse> updatePilot(@PathVariable Long id,
                                                       @Valid @RequestBody PilotUpdateRequest request) {
        return ResponseEntity.ok(pilotService.updatePilot(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePilot(@PathVariable Long id) {
        pilotService.deletePilot(id);
        return ResponseEntity.noContent().build();
    }
}
