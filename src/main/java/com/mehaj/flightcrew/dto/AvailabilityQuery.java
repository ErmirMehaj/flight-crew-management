package com.mehaj.flightcrew.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Shared query-parameter shape for the "list available X" endpoints
 * (aircraft/pilots/crew). Bound from query params (?departureTime=...
 * &arrivalTime=...) via Spring's implicit @ModelAttribute binding on a
 * GET request, and validated the same way a request body would be.
 */
@Getter
@Setter
public class AvailabilityQuery {

    @NotNull(message = "departureTime is required")
    private LocalDateTime departureTime;

    @NotNull(message = "arrivalTime is required")
    private LocalDateTime arrivalTime;

    @AssertTrue(message = "arrivalTime must be after departureTime")
    public boolean isArrivalAfterDeparture() {
        if (departureTime == null || arrivalTime == null) {
            return true;
        }
        return arrivalTime.isAfter(departureTime);
    }
}
