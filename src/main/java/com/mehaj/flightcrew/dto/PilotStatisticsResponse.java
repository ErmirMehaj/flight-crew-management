package com.mehaj.flightcrew.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PilotStatisticsResponse {

    private Long pilotId;
    private String firstName;
    private String lastName;
    private Double totalFlightHours;
    private long totalFlightsCompleted;
    private long upcomingFlightsScheduled;
}
