package com.mehaj.flightcrew.controller;

import com.mehaj.flightcrew.dto.AircraftCreateRequest;
import com.mehaj.flightcrew.dto.AircraftResponse;
import com.mehaj.flightcrew.dto.AircraftUpdateRequest;
import com.mehaj.flightcrew.dto.AvailabilityQuery;
import com.mehaj.flightcrew.service.AircraftService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/aircraft")
@RequiredArgsConstructor
@Tag(name = "Aircraft", description = "Manage the fleet")
public class AircraftController {

    private final AircraftService aircraftService;

    @PostMapping
    public ResponseEntity<AircraftResponse> createAircraft(@Valid @RequestBody AircraftCreateRequest request) {
        AircraftResponse created = aircraftService.createAircraft(request);
        return ResponseEntity.created(URI.create("/api/aircraft/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<AircraftResponse>> getAllAircraft() {
        return ResponseEntity.ok(aircraftService.getAllAircraft());
    }

    @GetMapping("/available")
    public ResponseEntity<List<AircraftResponse>> getAvailableAircraft(@Valid AvailabilityQuery query) {
        return ResponseEntity.ok(aircraftService.getAvailableAircraft(query.getDepartureTime(), query.getArrivalTime()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AircraftResponse> getAircraftById(@PathVariable Long id) {
        return ResponseEntity.ok(aircraftService.getAircraftById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AircraftResponse> updateAircraft(@PathVariable Long id,
                                                             @Valid @RequestBody AircraftUpdateRequest request) {
        return ResponseEntity.ok(aircraftService.updateAircraft(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAircraft(@PathVariable Long id) {
        aircraftService.deleteAircraft(id);
        return ResponseEntity.noContent().build();
    }
}
