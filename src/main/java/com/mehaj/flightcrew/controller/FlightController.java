package com.mehaj.flightcrew.controller;

import com.mehaj.flightcrew.dto.FlightCreateRequest;
import com.mehaj.flightcrew.dto.FlightResponse;
import com.mehaj.flightcrew.dto.FlightUpdateRequest;
import com.mehaj.flightcrew.entity.CrewPosition;
import com.mehaj.flightcrew.entity.PilotRank;
import com.mehaj.flightcrew.service.FlightAssignmentService;
import com.mehaj.flightcrew.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Tag(name = "Flights", description = "Manage flights, and assign aircraft/pilots/crew to them")
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

    @Operation(summary = "Assign an aircraft to a flight",
            description = "Fails with 409 if the aircraft is not ACTIVE or is already booked on an overlapping flight.")
    @PutMapping("/{id}/aircraft/{aircraftId}")
    public ResponseEntity<FlightResponse> assignAircraft(@PathVariable Long id, @PathVariable Long aircraftId) {
        return ResponseEntity.ok(flightAssignmentService.assignAircraft(id, aircraftId));
    }

    @Operation(summary = "Assign a pilot to a flight",
            description = "Fails with 409 if the pilot is not ACTIVE, is unavailable during the flight's "
                    + "schedule, or is already assigned to an overlapping flight.")
    @PostMapping("/{id}/pilots/{pilotId}")
    public ResponseEntity<FlightResponse> assignPilot(@PathVariable Long id,
                                                        @PathVariable Long pilotId,
                                                        @RequestParam PilotRank role) {
        return ResponseEntity.ok(flightAssignmentService.assignPilot(id, pilotId, role));
    }

    @Operation(summary = "Assign a crew member to a flight",
            description = "Fails with 409 if the crew member is not ACTIVE, is already assigned to an "
                    + "overlapping flight, or would exceed the configured weekly working-hour limit.")
    @PostMapping("/{id}/crew/{crewMemberId}")
    public ResponseEntity<FlightResponse> assignCrew(@PathVariable Long id,
                                                       @PathVariable Long crewMemberId,
                                                       @RequestParam CrewPosition role) {
        return ResponseEntity.ok(flightAssignmentService.assignCrew(id, crewMemberId, role));
    }

    @Operation(summary = "Mark a flight as completed",
            description = "Requires an assigned aircraft and at least one assigned pilot. Credits each "
                    + "assigned pilot's flight hours and logs a WorkHours record for each assigned crew member.")
    @PostMapping("/{id}/complete")
    public ResponseEntity<FlightResponse> completeFlight(@PathVariable Long id) {
        return ResponseEntity.ok(flightAssignmentService.completeFlight(id));
    }

    @Operation(summary = "Cancel a flight", description = "Only legal while the flight is SCHEDULED.")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<FlightResponse> cancelFlight(@PathVariable Long id) {
        return ResponseEntity.ok(flightAssignmentService.cancelFlight(id));
    }
}
