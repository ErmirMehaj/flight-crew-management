package com.mehaj.flightcrew.controller;

import com.mehaj.flightcrew.dto.FlightCreateRequest;
import com.mehaj.flightcrew.dto.FlightResponse;
import com.mehaj.flightcrew.dto.FlightUpdateRequest;
import com.mehaj.flightcrew.entity.CrewPosition;
import com.mehaj.flightcrew.entity.PilotRank;
import com.mehaj.flightcrew.service.FlightAssignmentService;
import com.mehaj.flightcrew.service.FlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;
    private final FlightAssignmentService flightAssignmentService;

    @PostMapping
    public ResponseEntity<FlightResponse> createFlight(@Valid @RequestBody FlightCreateRequest request) {
        FlightResponse created = flightService.createFlight(request);
        return ResponseEntity.created(URI.create("/api/flights/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<FlightResponse>> getAllFlights() {
        return ResponseEntity.ok(flightService.getAllFlights());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightResponse> getFlightById(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.getFlightById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlightResponse> updateFlight(@PathVariable Long id,
                                                         @Valid @RequestBody FlightUpdateRequest request) {
        return ResponseEntity.ok(flightService.updateFlight(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long id) {
        flightService.deleteFlight(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/aircraft/{aircraftId}")
    public ResponseEntity<FlightResponse> assignAircraft(@PathVariable Long id, @PathVariable Long aircraftId) {
        return ResponseEntity.ok(flightAssignmentService.assignAircraft(id, aircraftId));
    }

    @PostMapping("/{id}/pilots/{pilotId}")
    public ResponseEntity<FlightResponse> assignPilot(@PathVariable Long id,
                                                        @PathVariable Long pilotId,
                                                        @RequestParam PilotRank role) {
        return ResponseEntity.ok(flightAssignmentService.assignPilot(id, pilotId, role));
    }

    @PostMapping("/{id}/crew/{crewMemberId}")
    public ResponseEntity<FlightResponse> assignCrew(@PathVariable Long id,
                                                       @PathVariable Long crewMemberId,
                                                       @RequestParam CrewPosition role) {
        return ResponseEntity.ok(flightAssignmentService.assignCrew(id, crewMemberId, role));
    }
}
