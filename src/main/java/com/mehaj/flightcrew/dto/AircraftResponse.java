package com.mehaj.flightcrew.dto;

import com.mehaj.flightcrew.entity.AircraftStatus;
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
public class AircraftResponse {

    private Long id;
    private String tailNumber;
    private String model;
    private Integer capacity;
    private AircraftStatus status;
}
