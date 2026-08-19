package com.mehaj.flightcrew.dto;

import com.mehaj.flightcrew.entity.FlightStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightResponse {

    private Long id;
    private String flightNumber;
    private String departureAirport;
    private String arrivalAirport;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private FlightStatus status;

    // Null until an aircraft has been assigned via the dedicated action.
    private Long aircraftId;
    private String aircraftTailNumber;

    private List<PilotAssignmentSummary> pilots;
    private List<CrewAssignmentSummary> crew;
}
