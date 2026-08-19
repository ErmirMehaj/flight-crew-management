package com.mehaj.flightcrew.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Deliberately does NOT include status or aircraftId: status changes only
 * through the complete/cancel actions, and aircraft assignment only
 * through the assign-aircraft action. This DTO only covers the flight's
 * own schedule details.
 */
@Getter
@Setter
public class FlightUpdateRequest {

    @NotBlank(message = "Flight number is required")
    private String flightNumber;

    @NotBlank(message = "Departure airport is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Departure airport must be a 3-letter IATA code")
    private String departureAirport;

    @NotBlank(message = "Arrival airport is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Arrival airport must be a 3-letter IATA code")
    private String arrivalAirport;

    @NotNull(message = "Departure time is required")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time is required")
    private LocalDateTime arrivalTime;

    @AssertTrue(message = "Arrival time must be after departure time")
    public boolean isArrivalAfterDeparture() {
        if (departureTime == null || arrivalTime == null) {
            return true;
        }
        return arrivalTime.isAfter(departureTime);
    }
}
