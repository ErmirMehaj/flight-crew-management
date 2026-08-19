package com.mehaj.flightcrew.dto;

import com.mehaj.flightcrew.entity.PilotRank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lightweight roster entry embedded in FlightResponse. Deliberately not
 * the full PilotResponse -- a flight roster doesn't need a pilot's email
 * or license number, just who they are and what role they're filling on
 * this specific flight.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PilotAssignmentSummary {

    private Long pilotId;
    private String firstName;
    private String lastName;
    private PilotRank role;
}
