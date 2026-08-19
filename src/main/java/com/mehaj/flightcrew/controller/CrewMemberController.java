package com.mehaj.flightcrew.controller;

import com.mehaj.flightcrew.dto.AvailabilityQuery;
import com.mehaj.flightcrew.dto.CrewMemberCreateRequest;
import com.mehaj.flightcrew.dto.CrewMemberResponse;
import com.mehaj.flightcrew.dto.CrewMemberUpdateRequest;
import com.mehaj.flightcrew.service.CrewMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/crew-members")
@RequiredArgsConstructor
public class CrewMemberController {

    private final CrewMemberService crewMemberService;

    @PostMapping
    public ResponseEntity<CrewMemberResponse> createCrewMember(@Valid @RequestBody CrewMemberCreateRequest request) {
        CrewMemberResponse created = crewMemberService.createCrewMember(request);
        return ResponseEntity.created(URI.create("/api/crew-members/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<CrewMemberResponse>> getAllCrewMembers() {
        return ResponseEntity.ok(crewMemberService.getAllCrewMembers());
    }

    @GetMapping("/available")
    public ResponseEntity<List<CrewMemberResponse>> getAvailableCrew(@Valid AvailabilityQuery query) {
        return ResponseEntity.ok(crewMemberService.getAvailableCrew(query.getDepartureTime(), query.getArrivalTime()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrewMemberResponse> getCrewMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(crewMemberService.getCrewMemberById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CrewMemberResponse> updateCrewMember(@PathVariable Long id,
                                                                 @Valid @RequestBody CrewMemberUpdateRequest request) {
        return ResponseEntity.ok(crewMemberService.updateCrewMember(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCrewMember(@PathVariable Long id) {
        crewMemberService.deleteCrewMember(id);
        return ResponseEntity.noContent().build();
    }
}
